package com.etsubu.stonksbot.integration;

import com.etsubu.stonksbot.yahoo.TickerStorage;
import com.etsubu.stonksbot.yahoo.YahooConnector;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class YahooFinanceIntegrationTest {
    private YahooConnector yahooConnector;

    @Mock
    private TickerStorage tickerStorage;

    @BeforeEach
    public void init() {
        yahooConnector = new YahooConnectorImpl(tickerStorage);
        when(tickerStorage.findTicker(anyString())).thenReturn(Optional.empty());
    }

    @Test
    public void testTickerNotFound() throws IOException, InterruptedException {
        var ticker = yahooConnector.findTicker("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        assertTrue(ticker.isEmpty());
    }

    @Test
    public void testTickerFound() throws IOException, InterruptedException {
        var ticker = yahooConnector.findTicker("Microsoft");
        assertTrue(ticker.isPresent());
        assertEquals("MSFT", ticker.get().getTicker());
        assertTrue(ticker.get().getFullname().toLowerCase().contains("microsoft"));
    }

    @Test
    public void testQueryIntraPrice() throws IOException, InterruptedException {
        var info = yahooConnector.queryCurrentIntraPriceInfo("MSFT");
        assertTrue(info.isPresent());
        assertTrue(info.get().getCurrent().isGreaterThan(DecimalNum.valueOf(0)) && info.get().getCurrent().isLessThan(DecimalNum.valueOf(1000)));
        assertTrue(info.get().getPreviousClose().isGreaterThan(DecimalNum.valueOf(0)) && info.get().getPreviousClose().isLessThan(DecimalNum.valueOf(1000)));
        assertTrue(info.get().getVolume().isGreaterThanOrEqual(DecimalNum.valueOf(0)));
        assertTrue(info.get().getHigh().isGreaterThanOrEqual(info.get().getCurrent()));
        assertTrue(info.get().getLow().isLessThanOrEqual(info.get().getCurrent()));
        assertTrue(info.get().getChangePercent().isGreaterThan(DecimalNum.valueOf(0)) && info.get().getChangePercent().isLessThan(DecimalNum.valueOf(1000)));
    }

    @Test
    public void testQueryFinancials() throws IOException, InterruptedException {
        var info = yahooConnector.queryFundamentTimeSeries("MSFT", List.of("quarterlyTotalRevenue"));
        var financials = info.second;
        assertEquals(1, financials.size());
        var revenue = financials.get("quarterlyTotalRevenue");
        assertTrue(revenue.getValue().size() >= 4);
        assertTrue(revenue.getTimestamp().size() >= 4);
        revenue.getValue().forEach(x -> assertTrue(x.getReportedValue().isPresent() && DecimalNum.valueOf(x.getReportedValue().get().getRaw()).isPositiveOrZero()));
    }
}

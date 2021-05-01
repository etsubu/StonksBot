package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.yahoo.model.fundament.FundamentEntry;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.utility.Pair;
import com.etsubu.stonksbot.yahoo.StockName;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AltmanZScoreCommand extends Command {
    private static final String QUARTERLY_TOTAL_ASSETS = "quarterlyTotalAssets";
    private static final String QUARTERLY_TOTAL_LIABILITIES = "quarterlyTotalLiabilities";
    private static final String QUARTERLY_QUARTERLY_TOTAL_LIABILITIES_NET_MINORITY_INTEREST = "quarterlyTotalLiabilitiesNetMinorityInterest";
    private static final String QUARTERLY_TOTAL_REVENUE = "quarterlyTotalRevenue";
    private static final String QUARTERLY_WORKING_CAPITAL = "quarterlyWorkingCapital";
    private static final String QUARTERLY_RETAINED_EARNINGS = "quarterlyRetainedEarnings";
    private static final String TRAILING_MARKET_CAP = "trailingMarketCap";
    private static final String QUARTERLY_EBIT = "quarterlyEBIT";

    private static final Logger log = LoggerFactory.getLogger(FscoreCommand.class);
    private static final List<String> SCORE_COMPONENTS = Collections.unmodifiableList(List.of(
            QUARTERLY_TOTAL_ASSETS,
            QUARTERLY_TOTAL_LIABILITIES,
            QUARTERLY_WORKING_CAPITAL,
            QUARTERLY_RETAINED_EARNINGS,
            TRAILING_MARKET_CAP,
            QUARTERLY_TOTAL_REVENUE,
            QUARTERLY_QUARTERLY_TOTAL_LIABILITIES_NET_MINORITY_INTEREST,
            QUARTERLY_EBIT)
    );

    private final YahooConnectorImpl yahooConnector;

    public AltmanZScoreCommand(ConfigLoader configLoader, YahooConnectorImpl yahooConnector) {
        super(List.of("z", "zscore", "zluku"), configLoader, false);
        this.yahooConnector = yahooConnector;
    }

    private CommandResult buildResponse(Pair<StockName, Map<String, FundamentEntry>> response) {
        var entries = response.second;
        Optional<Num> ebitTTM = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_EBIT)));
        Optional<Num> revenueTTM = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_REVENUE)));
        Optional<Num> retainedEarningsTTM = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(QUARTERLY_RETAINED_EARNINGS)));
        Optional<Num> workingCapital = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(QUARTERLY_WORKING_CAPITAL)));
        Optional<Num> totalAssets = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS)));
        Optional<Num> totalLiabilities = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_LIABILITIES)));
        Optional<Num> totalLiabilitiesNet = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(QUARTERLY_QUARTERLY_TOTAL_LIABILITIES_NET_MINORITY_INTEREST)));
        if(totalLiabilities.isEmpty()) {
            totalLiabilities = totalLiabilitiesNet;
        }
        Optional<Num> marketcap = YahooConnectorImpl.getLatestValue(Optional.ofNullable(entries.get(TRAILING_MARKET_CAP)));
        log.info("{},{},{},{},{},{},{}", ebitTTM.isPresent(), revenueTTM.isPresent(), retainedEarningsTTM.isPresent(), workingCapital.isPresent(), totalAssets.isPresent(), totalLiabilities.isPresent(), marketcap.isPresent());
        if(ebitTTM.isEmpty() || revenueTTM.isEmpty() || retainedEarningsTTM.isEmpty() || totalAssets.isEmpty() || totalLiabilities.isEmpty() || marketcap.isEmpty() || workingCapital.isEmpty()) {
            return new CommandResult("Some financials could not be retrieved and the score cannot be calculated.", false);
        }
        Num A = workingCapital.get().dividedBy(totalAssets.get());
        Num B = retainedEarningsTTM.get().dividedBy(totalAssets.get());
        Num C = ebitTTM.get().dividedBy(totalAssets.get());
        Num D = marketcap.get().dividedBy(totalLiabilities.get());
        Num E = revenueTTM.get().dividedBy(totalAssets.get());
        // Z-Score = 1.2A + 1.4B + 3.3C + 0.6D + 1.0E
        Num score = (DecimalNum.valueOf(1.2).multipliedBy(A))
                .plus(DecimalNum.valueOf(1.4).multipliedBy(B))
                .plus(DecimalNum.valueOf(3.3).multipliedBy(C))
                .plus(DecimalNum.valueOf(0.6).multipliedBy(D))
                .plus(DecimalNum.valueOf(1.0).multipliedBy(E));
        StringBuilder builder = new StringBuilder("```\n").append(response.first.getFullname()).append(" - ").append(response.first.getTicker()).append('\n');
        builder.append("Z-Score: ").append(DoubleTools.round(score.doubleValue())).append("\n```");
        return new CommandResult(builder.toString(), true);
    }

    @Override
    public CommandResult exec(String command) {
        log.info("Requesting z-score for {}", command);
        try {
            Pair<StockName, Map<String, FundamentEntry>> response = yahooConnector.queryFundamentTimeSeries(command, SCORE_COMPONENTS);
            return buildResponse(response);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve key statistics for requested stock '{}'", command, e);
            return new CommandResult("Failed to retrieve key statistics for the requested stock", false);
        }
    }

    @Override
    public String help() {
        return "Calculates Altman Z-Score for the requested stock\n" +
                "Breakdown of the score formula https://www.investopedia.com/terms/a/altman.asp\n" +
                "Usage: !" + String.join("/", super.names) + " [stockname/ticker]\n" +
                "Example: !zscore microsoft";
    }
}
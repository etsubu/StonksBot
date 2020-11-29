package Core.Commands;

import Core.Configuration.ConfigLoader;
import Core.Utilities.DoubleTools;
import Core.Utilities.Pair;
import Core.YahooAPI.DataStructures.DataValue;
import Core.YahooAPI.DataStructures.FundamentalTimeSeries.FundaValue;
import Core.YahooAPI.DataStructures.FundamentalTimeSeries.FundamentEntry;
import Core.YahooAPI.StockName;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RatiosCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(RatiosCommand.class);
    private static final String MARKET_CAP = "trailingMarketCap";
    private static final String QUARTERLY_NET_INCOME = "quarterlyNetIncome";
    private static final String QUARTERLY_CURRENT_ASSETS = "quarterlyCurrentAssets";
    private static final String QUARTERLY_CURRENT_LIABILITIES = "quarterlyCurrentLiabilities";
    private static final String QUARTERLY_GROSS_PROFIT = "quarterlyGrossProfit";
    private static final String QUARTERLY_TOTAL_ASSETS = "quarterlyTotalAssets";
    private static final String QUARTERLY_TOTAL_REVENUE = "quarterlyTotalRevenue";
    private static final String TRAILING_SALES = "trailingPsRatio";
    private static final String QUARTERLY_FREE_CASH_FLOW = "quarterlyFreeCashFlow";

    private static final List<String> SCORE_COMPONENTS = Collections.unmodifiableList(List.of(
            MARKET_CAP,
            QUARTERLY_NET_INCOME,
            QUARTERLY_CURRENT_ASSETS,
            QUARTERLY_CURRENT_LIABILITIES,
            QUARTERLY_GROSS_PROFIT,
            QUARTERLY_TOTAL_ASSETS,
            QUARTERLY_TOTAL_REVENUE,
            QUARTERLY_FREE_CASH_FLOW,
            TRAILING_SALES)
    );
    private final YahooConnectorImpl yahooConnector;

    public RatiosCommand(YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("ratios", "r", "suhdeluvut"), configLoader, true);
        this.yahooConnector = yahooConnector;
    }


    private Optional<Num> sumLastFourQuarters(Optional<FundamentEntry> entry) {
        if(entry.isEmpty()) {
            log.info("Argument was empty, returning empty");
            return Optional.empty();
        }
        List<FundaValue> values = entry.get().getValue();
        if(values == null || values.size() < 4) {
            log.info("Value does not have enough quarters available to calculate TTM");
            return Optional.empty();
        }
        Num sum = PrecisionNum.valueOf(0);
        for(int i = 0; i < 4; i++) {
            Optional<DataValue> reportedValue = values.get(i).getReportedValue();
            if(reportedValue.isEmpty()) {
                log.info("Quarter's reported value was empty");
                return Optional.empty();
            }
            sum = sum.plus(PrecisionNum.valueOf(reportedValue.get().getRaw()));
        }
        return Optional.of(sum);
    }

    private CommandResult buildResponse(Pair<StockName, Map<String, FundamentEntry>> response) {
        var entries = response.getSecond();
        Optional<Num> netIncomeTTM = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_NET_INCOME)));
        Optional<Num> grossProfitTTM = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_GROSS_PROFIT)));
        Optional<Num> revenueTTM = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_REVENUE)));
        Optional<Num> freeCashFlow = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_FREE_CASH_FLOW)));
        Optional<FundamentEntry> marketcap = Optional.ofNullable(entries.get(MARKET_CAP));
        //Optional<Num> totalAssets = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS)));
        Optional<FundamentEntry> quarterlyTotalAssets = Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS));
        Optional<FundamentEntry> quarterlyCurrentAssets = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_ASSETS));
        Optional<FundamentEntry> quarterlyCurrentLiabilities = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_LIABILITIES));
        StringBuilder builder = new StringBuilder("```\n").append(response.getFirst().getFullname()).append(" - ").append(response.getFirst().getTicker()).append('\n');
        if(quarterlyCurrentAssets.isPresent() && quarterlyCurrentLiabilities.isPresent()
                && Optional.ofNullable(quarterlyCurrentAssets.get().getValue()).map(x -> x.size() >= 4).orElse(false)
                && Optional.ofNullable(quarterlyCurrentLiabilities.get().getValue()).map(x -> x.size() >= 4).orElse(false)) {
            Optional<DataValue> currentAssets = quarterlyCurrentAssets.get().getValue().get(0).getReportedValue();
            Optional<DataValue> currentLiabilities = quarterlyCurrentLiabilities.get().getValue().get(0).getReportedValue();
            if(currentAssets.isPresent() && currentLiabilities.isPresent()) {
                builder.append("Current ratio: ")
                        .append(DoubleTools.round(PrecisionNum.valueOf(currentAssets.get().getRaw())
                                .dividedBy(PrecisionNum.valueOf(currentLiabilities.get().getRaw()))
                                .doubleValue(), 2))
                        .append('\n');
            }
        }
        if(netIncomeTTM.isPresent() && quarterlyTotalAssets.isPresent()
                && Optional.ofNullable(quarterlyTotalAssets.get().getValue())
                    .map(x -> x.size() > 0).orElse(false)) {
            Optional<DataValue> assets = quarterlyTotalAssets.get().getValue().get(0).getReportedValue();
            assets.ifPresent(value -> builder.append("ROA: ").append(DoubleTools.round(netIncomeTTM.get()
                    .dividedBy(PrecisionNum.valueOf(value.getRaw()))
                    .multipliedBy(PrecisionNum.valueOf(100)).doubleValue(), 2))
                    .append("%\n"));
        }
        if(marketcap.isPresent() && freeCashFlow.isPresent()
                && Optional.ofNullable(marketcap.get().getValue())
                .map(x -> x.size() > 0).orElse(false)) {
            Optional<DataValue> marketcapValue = marketcap.get().getValue().get(0).getReportedValue();
            marketcapValue.ifPresent(x -> builder.append("FCF yield: ")
                    .append(DoubleTools.round(PrecisionNum.valueOf(x.getRaw()).dividedBy(freeCashFlow.get()).doubleValue(), 2))
                    .append(" / ").append(DoubleTools.round(freeCashFlow.get().dividedBy(PrecisionNum.valueOf(x.getRaw())).multipliedBy(PrecisionNum.valueOf(100)).doubleValue(), 2))
                    .append("%\n"));
        }
        if(grossProfitTTM.isPresent() && revenueTTM.isPresent()) {
            builder.append("Gross Profit Margin: ")
                    .append(DoubleTools.round(grossProfitTTM.get().dividedBy(revenueTTM.get()).multipliedBy(PrecisionNum.valueOf(100)).doubleValue(), 2))
                    .append("%\n");
        }
        Optional.ofNullable(entries.get(TRAILING_SALES)).ifPresent(x -> builder.append("P/S: ").append(x.getValue().get(0).getReportedValue().get().getFmt()).append('\n'));
        builder.append("```");
        return new CommandResult(builder.toString(), true);
    }

    @Override
    public CommandResult exec(String command) {
        log.info("Requesting ratios for {}", command);
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
        return "Retrieves different key ratio values for the requested stock\nUsage: !ratios [stockname/ticker]\nExample: !ratios msft";
    }
}

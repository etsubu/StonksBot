package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.DataValue;
import com.etsubu.stonksbot.yahoo.model.fundament.FundaValue;
import com.etsubu.stonksbot.yahoo.model.fundament.FundamentEntry;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import com.etsubu.stonksbot.utility.Pair;
import com.etsubu.stonksbot.yahoo.StockName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RatiosCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(RatiosCommand.class);
    private static final String MARKET_CAP = "trailingMarketCap";
    private static final String ENTERPRISE_VALUE = "trailingMarketCap";
    private static final String QUARTERLY_NET_INCOME = "quarterlyNetIncome";
    private static final String QUARTERLY_CURRENT_ASSETS = "quarterlyCurrentAssets";
    private static final String QUARTERLY_CURRENT_LIABILITIES = "quarterlyCurrentLiabilities";
    private static final String QUARTERLY_GROSS_PROFIT = "quarterlyGrossProfit";
    private static final String QUARTERLY_TOTAL_ASSETS = "quarterlyTotalAssets";
    private static final String QUARTERLY_TOTAL_REVENUE = "quarterlyTotalRevenue";
    private static final String TRAILING_SALES = "trailingPsRatio";
    private static final String TRAILING_PE = "trailingPeRatio";
    private static final String TRAILING_PB = "trailingPbRatio";
    private static final String QUARTERLY_FREE_CASH_FLOW = "quarterlyFreeCashFlow";
    private static final String QUARTERLY_EBIT = "quarterlyEBIT";
    private static final String QUARTERLY_EBITDA = "quarterlyNormalizedEBITDA";


    private static final List<String> SCORE_COMPONENTS = List.of(
            MARKET_CAP,
            ENTERPRISE_VALUE,
            QUARTERLY_NET_INCOME,
            QUARTERLY_CURRENT_ASSETS,
            QUARTERLY_CURRENT_LIABILITIES,
            QUARTERLY_GROSS_PROFIT,
            QUARTERLY_TOTAL_ASSETS,
            QUARTERLY_TOTAL_REVENUE,
            QUARTERLY_FREE_CASH_FLOW,
            TRAILING_SALES,
            TRAILING_PE,
            TRAILING_PB,
            QUARTERLY_EBIT,
            QUARTERLY_EBITDA);
    private final YahooConnectorImpl yahooConnector;

    public RatiosCommand(YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("ratios", "r", "suhdeluvut"), configLoader, true);
        this.yahooConnector = yahooConnector;
    }

    private Optional<Num> getLatestValue(Optional<FundamentEntry> entry) {
        if(entry.isEmpty()) {
            return Optional.empty();
        }
        Optional<List<FundaValue>> values = Optional.ofNullable(entry.get().getValue());
        if(values.map(List::isEmpty).orElse(true) || values.get().get(0).getReportedValue().isEmpty()) {
            return Optional.empty();
        }
        String rawValue = values.get().get(0).getReportedValue().get().getRaw();
        if(rawValue == null) {
            return Optional.empty();
        }
        return Optional.of(DecimalNum.valueOf(rawValue));
    }

    private void calculateRatio(Num base, Num value, StringBuilder response, String ratioName) {
        response.append(ratioName).append(DoubleTools.roundToFormat((base.dividedBy(value)).doubleValue()))
                .append(" - ").append(DoubleTools.roundToFormat((value.dividedBy(base)).multipliedBy(DecimalNum.valueOf(100)).doubleValue()))
                .append("%\n");
    }

    private CommandResult buildResponse(Pair<StockName, Map<String, FundamentEntry>> response) {
        var entries = response.getSecond();
        Optional<Num> netIncomeTTM = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_NET_INCOME)));
        Optional<Num> grossProfitTTM = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_GROSS_PROFIT)));
        Optional<Num> revenueTTM = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_REVENUE)));
        Optional<Num> freeCashFlow = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_FREE_CASH_FLOW)));
        Optional<Num> ebit = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_EBIT)));
        Optional<Num> ebitda = YahooConnectorImpl.sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_EBITDA)));
        Optional<Num> marketcap = getLatestValue(Optional.ofNullable(entries.get(MARKET_CAP)));
        Optional<Num> enterpriseValue = getLatestValue(Optional.ofNullable(entries.get(ENTERPRISE_VALUE)));
        Optional<Num> peRatio = getLatestValue(Optional.ofNullable(entries.get(TRAILING_PE)));
        Optional<Num> pbRatio = getLatestValue(Optional.ofNullable(entries.get(TRAILING_PB)));
        Optional<Num> psRatio = getLatestValue(Optional.ofNullable(entries.get(TRAILING_SALES)));
        Optional<FundamentEntry> quarterlyTotalAssets = Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS));
        Optional<FundamentEntry> quarterlyCurrentAssets = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_ASSETS));
        Optional<FundamentEntry> quarterlyCurrentLiabilities = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_LIABILITIES));

        StringBuilder builder = new StringBuilder("```\n").append(response.getFirst().getFullname()).append(" - ").append(response.getFirst().getTicker()).append('\n');

        enterpriseValue.ifPresent(num -> {
            revenueTTM.ifPresent(x -> calculateRatio(num, x, builder, "EV/Revenue:          "));
            ebitda.ifPresent(x -> calculateRatio(num, x, builder,     "EV/EBITDA:           "));
            ebit.ifPresent(x -> calculateRatio(num, x, builder,       "EV/EBIT:             "));
        });
        marketcap.ifPresent(num -> freeCashFlow.ifPresent(x -> calculateRatio(num, x, builder, "MCAP/FCF:            ")));

        if (grossProfitTTM.isPresent() && revenueTTM.isPresent()) {
            builder.append("Gross Profit Margin: ")
                    .append(DoubleTools.round(grossProfitTTM.get().dividedBy(revenueTTM.get()).multipliedBy(DecimalNum.valueOf(100)).doubleValue(), 2))
                    .append("%\n");
        }

        if (quarterlyCurrentAssets.isPresent() && quarterlyCurrentLiabilities.isPresent()
                && Optional.ofNullable(quarterlyCurrentAssets.get().getValue()).map(x -> x.size() >= 4).orElse(false)
                && Optional.ofNullable(quarterlyCurrentLiabilities.get().getValue()).map(x -> x.size() >= 4).orElse(false)) {
            Optional<DataValue> currentAssets = quarterlyCurrentAssets.get().getValue().get(0).getReportedValue();
            Optional<DataValue> currentLiabilities = quarterlyCurrentLiabilities.get().getValue().get(0).getReportedValue();
            if (currentAssets.isPresent() && currentLiabilities.isPresent()) {
                builder.append("Current ratio: ")
                        .append(DoubleTools.round(DecimalNum.valueOf(currentAssets.get().getRaw())
                                .dividedBy(DecimalNum.valueOf(currentLiabilities.get().getRaw()))
                                .doubleValue(), 2))
                        .append('\n');
            }
        }
        if (netIncomeTTM.isPresent() && quarterlyTotalAssets.isPresent()
                && Optional.ofNullable(quarterlyTotalAssets.get().getValue())
                .map(x -> x.size() > 0).orElse(false)) {
            Optional<DataValue> assets = quarterlyTotalAssets.get().getValue().get(0).getReportedValue();
            assets.ifPresent(value -> builder.append("ROA: ").append(DoubleTools.round(netIncomeTTM.get()
                    .dividedBy(DecimalNum.valueOf(value.getRaw()))
                    .multipliedBy(DecimalNum.valueOf(100)).doubleValue(), 2))
                    .append("%\n"));
        }

        psRatio.ifPresent(x -> builder.append("P/S: ").append(DoubleTools.roundToFormat(x.doubleValue())).append('\n'));
        peRatio.ifPresent(x -> builder.append("P/E: ").append(DoubleTools.roundToFormat(x.doubleValue())).append('\n'));
        pbRatio.ifPresent(x -> builder.append("P/B: ").append(DoubleTools.roundToFormat(x.doubleValue())).append('\n'));
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

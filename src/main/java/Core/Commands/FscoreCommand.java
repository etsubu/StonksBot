package Core.Commands;

import Core.Configuration.ConfigLoader;
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
public class FscoreCommand extends Command{
    private static final String QUARTERLY_NET_INCOME = "quarterlyNetIncome";
    private static final String QUARTERLY_OPERATING_CASH_FLOW = "quarterlyOperatingCashFlow";
    private static final String QUARTERLY_LONG_TERM_DEBT = "quarterlyLongTermDebt";
    private static final String QUARTERLY_CURRENT_ASSETS = "quarterlyCurrentAssets";
    private static final String QUARTERLY_CURRENT_LIABILITIES = "quarterlyCurrentLiabilities";
    private static final String QUARTERLY_SHARE_ISSUED = "quarterlyShareIssued";
    private static final String ANNUAL_GROSS_PROFIT = "annualGrossProfit";
    private static final String QUARTERLY_TOTAL_ASSETS = "quarterlyTotalAssets";
    private static final String QUARTERLY_TOTAL_REVENUE = "quarterlyTotalRevenue";
    private static final String ANNUAL_LONG_TERM_DEBT = "annualLongTermDebt";
    private static final String ANNUAL_TOTAL_REVENUE = "annualTotalRevenue";

    private static final Logger log = LoggerFactory.getLogger(FscoreCommand.class);
    private static final List<String> SCORE_COMPONENTS = Collections.unmodifiableList(List.of(
            QUARTERLY_NET_INCOME,
            QUARTERLY_OPERATING_CASH_FLOW,
            QUARTERLY_LONG_TERM_DEBT,
            QUARTERLY_CURRENT_ASSETS,
            QUARTERLY_CURRENT_LIABILITIES,
            QUARTERLY_SHARE_ISSUED,
            ANNUAL_GROSS_PROFIT,
            QUARTERLY_TOTAL_ASSETS,
            QUARTERLY_TOTAL_REVENUE,
            ANNUAL_LONG_TERM_DEBT,
            ANNUAL_TOTAL_REVENUE)
    );

    private final YahooConnectorImpl yahooConnector;

    public FscoreCommand(YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("fscore", "fluku", "f"), configLoader, false);
        this.yahooConnector = yahooConnector;
        log.info("Initialized f-score command");
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

    public CommandResult buildResponse(Pair<StockName, Map<String, FundamentEntry>> response) {
        int total = 0;
        int score = 0;
        var entries = response.second;
        Optional<Num> netIncomeTTM = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_NET_INCOME)));
        Optional<Num> opexTTM = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_OPERATING_CASH_FLOW)));
        Optional<Num> totalAssets = sumLastFourQuarters(Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS)));
        Optional<FundamentEntry> quarterlyTotalAssets = Optional.ofNullable(entries.get(QUARTERLY_TOTAL_ASSETS));
        Optional<FundamentEntry> quarterlyLongTermDebt = Optional.ofNullable(entries.get(QUARTERLY_LONG_TERM_DEBT));
        Optional<FundamentEntry> annualLongTermDebt = Optional.ofNullable(entries.get(ANNUAL_LONG_TERM_DEBT));
        Optional<FundamentEntry> quarterlyCurrentAssets = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_ASSETS));
        Optional<FundamentEntry> quarterlyCurrentLiabilities = Optional.ofNullable(entries.get(QUARTERLY_CURRENT_LIABILITIES));
        Optional<FundamentEntry> quarterlyShareIssued = Optional.ofNullable(entries.get(QUARTERLY_SHARE_ISSUED));
        Optional<FundamentEntry> annualGrossProfit = Optional.ofNullable(entries.get(ANNUAL_GROSS_PROFIT));
        Optional<FundamentEntry> quarterlyTotalRevenue = Optional.ofNullable(entries.get(QUARTERLY_TOTAL_REVENUE));
        Optional<FundamentEntry> annualTotalRevenue = Optional.ofNullable(entries.get(ANNUAL_TOTAL_REVENUE));
        StringBuilder builder = new StringBuilder("```\n").append(response.first.getFullname()).append(" - ").append(response.first.getTicker()).append('\n');
        if(netIncomeTTM.isPresent()) {
            total++;
            if(netIncomeTTM.get().isPositive()) {
                score++;
                builder.append("Positive net income: 1\n");
            } else {
                builder.append("Positive net income: 0\n");
            }
        }
        if(opexTTM.isPresent()) {
            total++;
            if(opexTTM.get().isPositive()) {
                score++;
                builder.append("Positive operating cash flow: 1\n");
            } else {
                builder.append("Positive operating cash flow: 0\n");
            }
        }
        if(netIncomeTTM.isPresent() && opexTTM.isPresent()) {
            total++;
            if(opexTTM.get().isGreaterThan(netIncomeTTM.get())) {
                score++;
                builder.append("Operating cash flow > net income: 1\n");
            } else {
                builder.append("Operating cash flow > net income: 0\n");
            }
        }
        if(totalAssets.isPresent() && netIncomeTTM.isPresent()) {
            Num roa = netIncomeTTM.get().dividedBy(totalAssets.get());
            total++;
            if(roa.isPositive()) {
                score++;
                builder.append("Positive ROA: 1\n");
            } else {
                builder.append("Positive ROA: 0\n");
            }
        }
        if(quarterlyLongTermDebt.isPresent() && quarterlyLongTermDebt.get().getValue() != null && quarterlyLongTermDebt.get().getValue().size() >= 4) {
            Optional<DataValue> currentValue = quarterlyLongTermDebt.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousValue = quarterlyLongTermDebt.get().getValue().get(3).getReportedValue();
            if(currentValue.isPresent() && previousValue.isPresent()) {
                Num currentDebt = PrecisionNum.valueOf(currentValue.get().getRaw());
                Num previousDebt = PrecisionNum.valueOf(previousValue.get().getRaw());
                total++;
                if(currentDebt.isLessThan(previousDebt)) {
                    score++;
                    builder.append("Decrease of long term debt: 1\n");
                } else {
                    builder.append("Decrease of long term debt: 0\n");
                }
            }
        } else if(annualLongTermDebt.isPresent() && annualLongTermDebt.get().getValue() != null && annualLongTermDebt.get().getValue().size() >= 2) {
            Optional<DataValue> currentValue = annualLongTermDebt.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousValue = annualLongTermDebt.get().getValue().get(1).getReportedValue();
            if(currentValue.isPresent() && previousValue.isPresent()) {
                Num currentDebt = PrecisionNum.valueOf(currentValue.get().getRaw());
                Num previousDebt = PrecisionNum.valueOf(previousValue.get().getRaw());
                total++;
                if(currentDebt.isLessThan(previousDebt)) {
                    score++;
                    builder.append("Decrease of long term debt: 1\n");
                } else {
                    builder.append("Decrease of long term debt: 0\n");
                }
            }
        }
        if(quarterlyCurrentAssets.isPresent() && quarterlyCurrentLiabilities.isPresent()
                && Optional.ofNullable(quarterlyCurrentAssets.get().getValue()).map(x -> x.size() >= 4).orElse(false)
                && Optional.ofNullable(quarterlyCurrentLiabilities.get().getValue()).map(x -> x.size() >= 4).orElse(false)) {
            Optional<DataValue> currentAssets = quarterlyCurrentAssets.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousAssets = quarterlyCurrentAssets.get().getValue().get(3).getReportedValue();
            Optional<DataValue> currentLiabilities = quarterlyCurrentLiabilities.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousLiabilities = quarterlyCurrentLiabilities.get().getValue().get(3).getReportedValue();
            if(currentAssets.isPresent() && previousAssets.isPresent() && currentLiabilities.isPresent() && previousLiabilities.isPresent()) {
                Num previousCurrentRatio = PrecisionNum.valueOf(previousAssets.get().getRaw()).dividedBy(PrecisionNum.valueOf(previousLiabilities.get().getRaw()));
                Num currentCurrentRatio = PrecisionNum.valueOf(previousAssets.get().getRaw()).dividedBy(PrecisionNum.valueOf(previousLiabilities.get().getRaw()));
                total++;
                if(currentCurrentRatio.isGreaterThan(previousCurrentRatio)) {
                    score++;
                    builder.append("Higher current ratio: 1\n");
                } else {
                    builder.append("Higher current ratio: 0\n");
                }
            }

        }
        if(quarterlyShareIssued.isPresent() && quarterlyShareIssued.get().getValue() != null && quarterlyShareIssued.get().getValue().size() >= 4) {
            Optional<DataValue> currentSharesIssued = quarterlyShareIssued.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousSharesIssued = quarterlyShareIssued.get().getValue().get(3).getReportedValue();
            if(currentSharesIssued.isPresent() && previousSharesIssued.isPresent()) {
                total++;
                if(PrecisionNum.valueOf(currentSharesIssued.get().getRaw()).isLessThanOrEqual(PrecisionNum.valueOf(previousSharesIssued.get().getRaw()))) {
                    score++;
                    builder.append("No new shares issued: 1\n");
                } else {
                    builder.append("No new shares issued: 0\n");
                }
            }
        }
        if(annualGrossProfit.isPresent() && annualGrossProfit.get().getValue() != null && annualGrossProfit.get().getValue().size() >= 2
                && annualTotalRevenue.isPresent() && annualTotalRevenue.get().getValue() != null && annualTotalRevenue.get().getValue().size() >= 2) {
            Optional<DataValue> currentGrossProfit = annualGrossProfit.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousGrossProfit = annualGrossProfit.get().getValue().get(1).getReportedValue();
            Optional<DataValue> currentTotalRevenue = annualTotalRevenue.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousTotalRevenue = annualTotalRevenue.get().getValue().get(1).getReportedValue();
            if(currentGrossProfit.isPresent() && previousGrossProfit.isPresent() && currentTotalRevenue.isPresent() && previousTotalRevenue.isPresent()) {
                total++;
                Num currentGrossProfitMargin = PrecisionNum.valueOf(currentGrossProfit.get().getRaw()).dividedBy(PrecisionNum.valueOf(currentTotalRevenue.get().getRaw()));
                Num previousGrossProfitMargin = PrecisionNum.valueOf(previousGrossProfit.get().getRaw()).dividedBy(PrecisionNum.valueOf(previousTotalRevenue.get().getRaw()));
                if(currentGrossProfitMargin.isGreaterThan(previousGrossProfitMargin)) {
                    score++;
                    builder.append("Higher gross profit margin: 1\n");
                } else {
                    builder.append("Higher gross profit margin: 0\n");
                }
            }
        }
        if(quarterlyTotalRevenue.isPresent() && quarterlyTotalRevenue.get().getValue() != null && quarterlyTotalRevenue.get().getValue().size() >= 4
                && quarterlyTotalAssets.isPresent() && quarterlyTotalAssets.get().getValue() != null && quarterlyTotalAssets.get().getValue().size() >= 4) {
            Optional<DataValue> currentTotalRevenue = quarterlyTotalRevenue.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousTotalRevenue = quarterlyTotalRevenue.get().getValue().get(3).getReportedValue();
            Optional<DataValue> currentTotalAssets = quarterlyTotalAssets.get().getValue().get(0).getReportedValue();
            Optional<DataValue> previousTotalAssets = quarterlyTotalAssets.get().getValue().get(3).getReportedValue();
            if(currentTotalRevenue.isPresent() && previousTotalRevenue.isPresent() && currentTotalAssets.isPresent() && previousTotalAssets.isPresent()) {
                Num currentAssetTurnover = PrecisionNum.valueOf(currentTotalRevenue.get().getRaw()).dividedBy(PrecisionNum.valueOf(currentTotalAssets.get().getRaw()));
                Num previousAssetTurnover = PrecisionNum.valueOf(previousTotalRevenue.get().getRaw()).dividedBy(PrecisionNum.valueOf(previousTotalAssets.get().getRaw()));
                total++;
                if(currentAssetTurnover.isGreaterThan(previousAssetTurnover)) {
                    score++;
                    builder.append("Higher asset turnover ratio: 1\n");
                } else {
                    builder.append("Higher asset turnover ratio: 0\n");
                }
            }
        }
        return new CommandResult(builder.append("Score: ").append(score).append("\nChecked criterias: ").append(total).append("\n```").toString(), true);
    }

    @Override
    public CommandResult exec(String command) {
        log.info("Requesting statistics for {}", command);
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
        return "Calculates Piotroski Score also known as \"f-score\" for the requested stock\n" +
                "Breakdown of the score formula https://www.investopedia.com/terms/p/piotroski-score.asp\n" +
                "Usage: !" + String.join("/", super.names) + " [stockname/ticker]\n" +
                "Example: !fscore microsoft";
    }
}

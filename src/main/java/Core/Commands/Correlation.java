package Core.Commands;

import Core.Utilities.DoubleTools;
import Core.Utilities.Statistics;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Correlation command calculates the correlation between two assets for the given timeframe
 */
public class Correlation extends Command {
    private static final Logger log = LoggerFactory.getLogger(Correlation.class);
    private final YahooConnectorImpl api;

    /**
     * Initializes Correlation
     */
    public Correlation(YahooConnectorImpl api) {
        super("correlation");
        this.api = api;
    }

    private String getCorrelationForYears(double[] values1, double[] values2, int years) {
        int candleCount = years * 253;
        if(values1.length > candleCount && values2.length > candleCount) {
            values1 = Arrays.copyOfRange(values1, values1.length - candleCount, values1.length);
            values2 = Arrays.copyOfRange(values2, values2.length - candleCount, values2.length);
            double correlation = DoubleTools.round(Statistics.correlation(values1, values2), 2);
            if(correlation == -2) {
                return "There was an internal error while calculating correlation";
            }
            return String.valueOf(correlation);
        } else {
            return "There was not enough data points available to calculate correlation";
        }
    }

    @Override
    public CommandResult execute(String command) {
        if (command == null || command.isEmpty()) {
            return new CommandResult("Received empty parameters for Correlation!", false);
        }
        String[] parts = command.split("-");
        if(parts.length < 2) {
            return new CommandResult("Did not received 2 parameters for correlation!", false);
        }
        return new CommandResult("Not implemented yet!", true);
        /*Asset asset = exhanges.findCompany(parts[0].trim());
        Asset asset2 = exhanges.findCompany(parts[1].trim());
        if(asset == null || asset2 == null) {
            return new CommandResult("Could not find both assets!", false);
        }
        try {
            PriceCandle[] candles1 = api.queryPriceHistory(asset, AlphaVantageConnector.DAILY_ADJUSTED, true);
            PriceCandle[] candles2 = api.queryPriceHistory(asset2, AlphaVantageConnector.DAILY_ADJUSTED, true);
            double[] values1 = PriceCandle.getValues(candles1, candles2, PriceCandle.ADJUSTED_CLOSE);
            double[] values2 = PriceCandle.getValues(candles2, candles1, PriceCandle.ADJUSTED_CLOSE);
            long start = System.currentTimeMillis();
            String correlation = "Correlation between assets " + asset.getName() + " - " + asset2.getName() +
                    ": \n1 year:   " + getCorrelationForYears(values1, values2, 1) +
                    "\n3 years:  " + getCorrelationForYears(values1, values2, 3) +
                    "\n5 years:  " + getCorrelationForYears(values1, values2, 3) +
                    "\n10 years: " + getCorrelationForYears(values1, values2, 10);
            log.info("Calculated correlations in " + (System.currentTimeMillis() - start) + " millis");
            return new CommandResult(correlation, true);
        } catch (IOException e) {
            return new CommandResult("Failed to query asset prices!", false, e);
        } catch (ParseException e) {
            return new CommandResult("JSON parsing error!", false, e);
        }*/
    }

    @Override
    public String help() {
        return "Usage !correlation asset1 - asset2\nExample: !correlation nokia - microsoft";
    }
}

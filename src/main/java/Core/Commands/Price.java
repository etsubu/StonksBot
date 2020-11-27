package Core.Commands;

import Core.Configuration.ConfigLoader;
import Core.YahooAPI.DataStructures.AssetPriceIntraInfo;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Used for querying latest price info for a single stock
 * @author etsubu
 * @version 29 Aug 2018
 *
 */
@Component
public class Price extends Command {
    private static final Logger log = LoggerFactory.getLogger(Price.class);
    private final YahooConnectorImpl api;
    
    /**
     * Initializes Price
     * @param api AlphaVantageConnector for querying information
     */
    public Price(YahooConnectorImpl api, ConfigLoader configLoader) {
        super(List.of("price", "hinta", "p"), configLoader, true);
        this.api = api;
        log.info("Initialized price command");
    }

    @Override
    public CommandResult exec(String command) {
        log.info("Querying asset with: " + command);
        try {
            Optional<AssetPriceIntraInfo> info = api.queryCurrentIntraPriceInfo(command);
            if(info.isEmpty()) {
                return new CommandResult("Failed to request intraday info", false);
            }
            return new CommandResult(info.get().toString(), true);
        } catch (IOException | InterruptedException e) {
            return new CommandResult("Did not find a stock with they keyword '" + command + "'", false, e);
        }
    }

    @Override
    public String help() {
        return "Displays intraday price data for the given stock\nUsage !" + String.join("/", super.names)
                + " [stockname/ticker]\nExample: !price msft";
    }

}

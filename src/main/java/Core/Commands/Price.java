package Core.Commands;

import Core.YahooAPI.DataStructures.AssetPriceIntraInfo;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public Price(YahooConnectorImpl api) {
        super("price");
        this.api = api;
        log.info("Initialized price command");
    }

    @Override
    public CommandResult execute(String command) {
        log.info("Querying asset with: " + command);
        try {
            Optional<AssetPriceIntraInfo> info = api.queryCurrentIntraPriceInfo(command);
            if(info.isEmpty()) {
                return new CommandResult("Failed to request intraday info", false);
            }
            return new CommandResult(info.get().toString(), true);
        } catch (IOException | InterruptedException e) {
            return new CommandResult("Connection error to the API! Maybe the site is down?", false, e);
        }
    }

    @Override
    public String help() {
        return "Usage !price asset\nExample: !price nokia";
    }

}

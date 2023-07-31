package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.yahoo.YahooConnector;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Used for querying latest price info for a single stock
 *
 * @author etsubu
 * @version 29 Aug 2018
 */
@Component
public class Price extends Command {
    private static final Logger log = LoggerFactory.getLogger(Price.class);
    private final YahooConnector api;

    /**
     * Initializes Price command
     *
     * @param api          yahoo finance api connector
     * @param configLoader Configuration loader
     */
    public Price(YahooConnector api, ConfigLoader configLoader) {
        super(List.of("price", "hinta", "p"), configLoader, true);
        this.api = api;
        log.info("Initialized price command");
    }

    @Override
    public CommandResult exec(CommandContext context) {
        String command = context.getMessage();
        if (command.isBlank()) {
            return new CommandResult("You need to specify stock name to query, see !help price", false);
        }
        log.info("Querying asset with: " + command);
        try {
            Optional<AssetPriceIntraInfo> info = api.queryCurrentIntraPriceInfo(command);
            if (info.isEmpty()) {
                return new CommandResult("Failed to request intraday info", false);
            }
            return new CommandResult(info.get().toString(), true);
        } catch (IOException | InterruptedException e) {
            return new CommandResult(false, false, "Did not find a stock with they keyword '" + command + "'", e);
        }
    }

    @Override
    public String help() {
        return "Displays intraday price data for the given stock\nUsage !" + String.join("/", super.names)
                + " [stockname/ticker]\nExample: !price msft";
    }

}

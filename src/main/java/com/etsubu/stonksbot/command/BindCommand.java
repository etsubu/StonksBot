package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.yahoo.TickerStorage;
import com.etsubu.stonksbot.yahoo.YahooConnector;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import com.etsubu.stonksbot.yahoo.StockName;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Command for binding keywords to tickers
 */
@Component
public class BindCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(BindCommand.class);
    private final YahooConnector yahooConnector;
    private final TickerStorage storage;

    public BindCommand(YahooConnector yahooConnector, TickerStorage storage, ConfigLoader configLoader) {
        super(List.of("bind", "sido"), configLoader, false);
        this.yahooConnector = yahooConnector;
        this.storage = storage;
        log.info("Bind command initialized");
    }

    @Override
    public CommandResult exec(CommandContext context) {
        String command = context.getMessage();
        String[] parts = command.split(" ");
        if (parts.length < 2) {
            return new CommandResult("You need to provide ticker and keyword for the command. See !help bind for more info", false);
        }
        String ticker = parts[0];
        String keyword = command.substring(command.indexOf(' ') + 1);
        try {
            Optional<StockName> name = yahooConnector.findTicker(ticker);
            if (name.isEmpty()) {
                log.error("Failed to find stock with ticker {}", ticker);
                return new CommandResult("Could not find any stock with ticker " + ticker + " please double check the name", false);
            }
            storage.setShortcut(keyword.replaceAll(":", ""), name.get());
            return new CommandResult("Bound keyword '" + keyword + "' to ticker '" + ticker + "'", true);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to find stock with ticker {}", ticker, e);
            return new CommandResult("Failed to communicate with data provider", false);
        }
    }

    @Override
    public String help() {
        return "Binds a stock ticker to given keyword to improve search experience. \nUsage: !bind [ticker] [keyword]\nExample: !bind ^omxh25 omxh25";
    }
}

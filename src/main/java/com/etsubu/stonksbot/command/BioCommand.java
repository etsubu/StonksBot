package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.yahoo.model.AssetProfile;
import com.etsubu.stonksbot.yahoo.model.DataResponse;
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
 * Used for retrieving a description of a given company
 * @author etsubu
 */
@Component
public class BioCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(BioCommand.class);
    private static final CommandResult errorResponse = new CommandResult("Failed to retrieve bio for the company", false);

    private final YahooConnectorImpl yahooConnector;
    /**
     * Initializes Command
     */
    public BioCommand(YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("bio", "kuvaus", "b"), configLoader, true);
        this.yahooConnector = yahooConnector;
        log.info("Initialized bio command");
    }

    private String buildResponse(AssetProfile assetProfile, StockName name) {
        StringBuilder builder = new StringBuilder();
        builder.append("```");
        Optional.ofNullable(assetProfile.getLongBusinessSummary()).ifPresentOrElse(builder::append, () -> builder.append("Business description is missing"));
        builder.append("```");
        return builder.toString();
    }

    @Override
    public CommandResult exec(CommandContext context) {
        String command = context.getMessage();
        if(command.isBlank()) {
            return new CommandResult("You need to specify stock name to query, see !help " + this.names.get(0), false);
        }
        try {
            Optional<DataResponse> response = yahooConnector.queryData(command, YahooConnectorImpl.ASSET_PROFILE);
            return response.map(x -> x.getAssetProfile()
                    .map(y -> new CommandResult(buildResponse(y, x.getName()), true)).orElse(errorResponse))
                    .orElse(errorResponse);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve calendar events for ticker", e);
        }
        return errorResponse;
    }

    @Override
    public String help() {
        return "Displays a business description of the requested company\n"
                + "Usage: !" + String.join("/", super.names) + " [stockname/ticker]\n"
                + "Example: !bio msft";
    }
}

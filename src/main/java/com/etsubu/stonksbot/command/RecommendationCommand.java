package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.inderes.InderesConnector;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RecommendationCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(RecommendationCommand.class);
    private final InderesConnector inderesConnector;
    private final YahooConnectorImpl yahooConnector;

    public RecommendationCommand(InderesConnector inderesConnector, YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("suositus", "recommendation"), configLoader, true);
        this.inderesConnector = inderesConnector;
        this.yahooConnector = yahooConnector;
    }

    private Optional<RecommendationEntry> filterEntry(Set<RecommendationEntry> entries, String name) {
        Optional<RecommendationEntry> entry = entries.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst();
        if(entry.isPresent()) {
            return entry;
        }
        return entries.stream().filter(x -> x.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst();
    }

    private String buildRecommendationDisplay(RecommendationEntry entry) {
        Optional<AssetPriceIntraInfo> price = Optional.empty();
        try {
            price = yahooConnector.queryCurrentIntraPriceInfo(entry.getIsin());
        } catch (IOException e) {
            log.error("Connection to yahoo finance failed", e);
        } catch (InterruptedException e) {
            log.error("Connection to yahoo finance timed out", e);
        }
        DecimalNum targetPrice = DecimalNum.valueOf(entry.getTarget());
        StringBuilder builder = new StringBuilder();
        builder.append("```\n(Inderes)\n");
        builder.append("Nimi: ").append(entry.getName()).append('\n');
        builder.append("Suosituksen päivämäärä: ").append(entry.getDate()).append('\n');
        builder.append("Tavoitehinta: ").append(entry.getTarget()).append(entry.getCurrency()).append('\n');
        price.ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(entry.getCurrency())
                .append("\nNousuvara: ")
                        .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                        .append("%\n"));
        builder.append("Suositus: ").append(entry.getRecommendationText()).append('\n');
        builder.append("Riski: ").append(entry.getRisk()).append("```");
        return builder.toString();
    }

    @Override
    public CommandResult exec(CommandContext context) {
        String command = context.getMessage();
        if(command.isEmpty()) {
            return new CommandResult("You must provide OMXH/first north stock name that inderes follows", false);
        }
        try {
            Set<RecommendationEntry> entries = inderesConnector.queryRecommendations();
            String[] parts = command.split(" ");
            if(parts[0].equals("filter")) {
                return new CommandResult("Not implemented yet", false);
            } else {
                Optional<RecommendationEntry> entry = filterEntry(entries, command);
                return entry.map(x -> new CommandResult(buildRecommendationDisplay(x), true))
                        .orElseGet(() -> new CommandResult("Could not find recommendation for the requested stock '" + command + "'. Probably not followed by inderes?", false));
            }
        } catch (IOException e) {
            log.error("Connection to inderes failed", e);
            return new CommandResult("Connection to inderes API failed, try again later", false);
        } catch (InterruptedException e) {
            log.error("Connection to inderes timed out", e);
            return new CommandResult("Request to inderes timed out, try again later", false);
        }
    }

    @Override
    public String help() {
        return "Queries price targets and recommendations for a stock followed by inderes\n"
                + "Usage: !" + String.join("/", super.names) + " [stockname]\n"
                + "Example: !" + super.names.get(0) + " neste";
    }
}

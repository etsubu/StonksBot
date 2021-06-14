package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Creates personalized links for users to access voting poll
 * @author etsubu
 */
@Component
public class VoteLinkCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(AboutCommand.class);
    /**
     * Initializes the author command
     */
    public VoteLinkCommand(ConfigLoader configLoader) {
        super(List.of("votelink", "äänestyslinkki"), configLoader, true);
        log.info("Initialized {}", AboutCommand.class.getName());
    }

    private static int countOccurences(String message, String substring) {
        if(message == null) {
            return 0;
        }
        int count = 0;
        int index = message.indexOf(substring);
        while(index != -1 && index < message.length()) {
            count++;
            index = message.indexOf(substring, index + substring.length());
        }
        return count;
    }

    private static String replaceInstances(String template, String substring, String[] replacements) {
        String pattern = Pattern.quote(substring);
        String builder = template;
        for(String s : replacements) {
            builder = builder.replaceFirst(pattern, URLEncoder.encode(s, StandardCharsets.UTF_8));
        }
        return builder;
    }

    @Override
    public CommandResult exec(CommandContext context) {
        Optional<String> votelinkTemplate = context.getServerConfig().flatMap(x -> Optional.ofNullable(x.getVotelinkTemplate()));
        if(votelinkTemplate.isEmpty()) {
            return new CommandResult("There are no active vote polls configured for the server.", false);
        }

        if(countOccurences(votelinkTemplate.get(), "{}") != 2) {
            log.error("Invalid votelink template '{}'", votelinkTemplate.get());
            return new CommandResult("There are no active vote polls configured for the server.", false);
        }
        String[] tag = context.getSender().getAsTag().split("#");
        tag[0] = "pitkä nimi";
        if(tag.length != 2) {
            log.error("User tag format has changed '{}'", context.getSender().getAsTag());
            return new CommandResult("There are no active vote polls configured for the server.", false);
        }
        String personalized = replaceInstances(votelinkTemplate.get(), "{}", tag);
        return CommandResult.builder()
                .response("Voting link: " + personalized)
                .succeeded(true)
                .respondWithDM(true)
                .build();
    }

    @Override
    public String help() {
        return "Displays the application name, version number, author and link to source code"
                + "\n\tUsage: !" + String.join("/", super.names);
    }
}

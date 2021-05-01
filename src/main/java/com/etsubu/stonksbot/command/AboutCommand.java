package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.utility.ArchiveTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Command for retrieving "about" text of this bot
 * @author etsubu
 */
@Component
public class AboutCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(AboutCommand.class);
    private final CommandResult result;

    /**
     * Initializes the author command
     */
    public AboutCommand(ConfigLoader configLoader) {
        super(List.of("about", "tietoja"), configLoader, true);
        result = new CommandResult("StonksBot - " + ArchiveTools.getApplicationVersion().orElse("Unknown version") +
                "\nAuthor Jarre Leskinen"
                + "\nSource code and documentation: https://github.com/etsubu/StonksBot", true);
        log.info("Initialized {}", AboutCommand.class.getName());
    }

    @Override
    public CommandResult exec(String command) {
        return result;
    }

    @Override
    public String help() {
        return "Displays the application name, version number, author and link to source code"
                + "\n\tUsage: !" + String.join("/", super.names);
    }
}

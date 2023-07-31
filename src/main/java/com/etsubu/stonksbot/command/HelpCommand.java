package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Displays help text for the requested command.
 *
 * @author etsubu
 */
@Component
public class HelpCommand extends Command implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(HelpCommand.class);
    private ApplicationContext applicationContext;

    /**
     * Initializes Help command
     */
    public HelpCommand(ConfigLoader configLoader) {
        super(List.of("help", "apua"), configLoader, true);
        log.info("Constructed Help bean");
    }

    @Override
    public CommandResult exec(CommandContext context) {
        String command = context.getMessage();
        if (applicationContext == null) {
            return new CommandResult("Still initializing commands, try again in a bit", true);
        }
        try {
            if (command == null || command.isEmpty() || command.isBlank()) {
                return new CommandResult(help(), false);
            }
            CommandHandler commandHandler = applicationContext.getBean(CommandHandler.class);
            return commandHandler.getCommand(command.toLowerCase())
                    .map(x -> new CommandResult(x.help(), true))
                    .orElseGet(() -> new CommandResult("Failed to find a command with name \"" + command + "\"", false));
        } catch (Exception e) {
            return new CommandResult("Still initializing commands, try again in a bit", true);
        }
    }

    @Override
    public String help() {
        return "Prints help text describing what the given command does and how to use it\n"
                + "Example !help price";
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

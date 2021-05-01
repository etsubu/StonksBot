package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListCommand extends Command implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    /**
     * Initializes Command
     */
    public ListCommand(ConfigLoader configLoader) {
        super(List.of("commands", "komennot"), configLoader, true);
    }

    @Override
    public CommandResult exec(String command) {
        if(applicationContext == null) {
            return new CommandResult("Not yet initialized, wait a moment and try again", true);
        }
        Map<String, Command> cmds = applicationContext.getBeansOfType(Command.class);
        StringBuilder builder = new StringBuilder();
        for(Command cmd : cmds.values()) {
            builder.append('!')
                    .append(String.join("/", cmd.getNames()))
                    .append('\n');
        }
        return new CommandResult(builder.toString(), true);
    }

    @Override
    public String help() {
        return "Lists all available commands\nUsage: !" + String.join("/", super.names);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

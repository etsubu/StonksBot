package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.EventConfig;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.services.DatabaseService.ItemStorage;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command that users can use for registering to an event
 */
@Component
public class EventCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(EventCommand.class);
    private static final String DEFAULT_EVENT_TITLE = "Default event";
    private final ItemStorage itemStorage;

    /**
     * Initializes the author command
     */
    public EventCommand(ConfigLoader configLoader, ItemStorage itemStorage) {
        super(List.of("event"), configLoader, true);
        this.itemStorage = itemStorage;
        log.info("Initialized {}", EventCommand.class.getName());
    }

    private CommandResult register(CommandContext context, EventConfig config) {
        if(Optional.ofNullable(config.getMax()).map(x -> itemStorage.entries(context.getGuild().get().getId()) >= x).orElse(false)) {
            return new CommandResult("Event is already full!", true);
        }
        if (!hasRole(context, config)) return new CommandResult("Registering is not yet enabled for your role!", false);
        String username = context.getSender().getName();
        String userId = context.getSender().getId();
        if(!itemStorage.addEntry(context.getGuild().get().getId(), userId, Map.of("username", username))) {
            return new CommandResult("Failed to register to event.", false);
        }
        return new CommandResult("Registered to event!", true);
    }

    private boolean hasRole(CommandContext context, EventConfig config) {
        Set<String> userRoleIds = context.getUserRoles().stream().map(ISnowflake::getId).collect(Collectors.toSet());
        return Optional.ofNullable(config.getAllowedGroupIds()).map(x -> x.stream().anyMatch(userRoleIds::contains)).orElse(false);
    }

    private CommandResult unregister(CommandContext context, EventConfig config) {
        String userId = context.getSender().getId();
        if (!hasRole(context, config)) return new CommandResult("Registering is not yet enabled for your role!", false);
        itemStorage.removeEntry(context.getGuild().get().getId(), userId);
        return new CommandResult("Unregistered from event!", true);
    }

    @Override
    public CommandResult exec(CommandContext context) {
        if(context.getGuild().isEmpty()) {
            return new CommandResult("You must use this command in a server that has event available!", false);
        }
        String guildId = context.getGuild().get().getId();
        Optional<ServerConfig> serverConfig = configLoader.getConfig().getServerConfig(guildId);
        if(serverConfig.map(ServerConfig::getRegistration).map(x -> x.getEnabled() == null || !x.getEnabled()).orElse(true)) {
            return new CommandResult("No active events scheduled for this server!", false);
        }
        EventConfig event = serverConfig.get().getRegistration();
        if(context.getMessage().equals("register")) {
            return register(context, event);
        } else if (context.getMessage().equals("unregister")) {
            return unregister(context, event);
        } else if(context.getMessage().isEmpty()) {
            return new CommandResult("Active event scheduled \"" + Optional.ofNullable(event.getTitle()).orElse(DEFAULT_EVENT_TITLE) + "\"", true);
        }
        return new CommandResult(help(), false);
    }

    @Override
    public String help() {
        return "Allows registering to an event."
                + "\n\tUsage: !" + String.join("/", super.names) + " register/unregister";
    }
}

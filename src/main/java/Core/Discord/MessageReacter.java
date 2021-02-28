package Core.Discord;

import Core.Configuration.ConfigLoader;
import Core.Configuration.Reaction;
import Core.Configuration.ServerConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Reacts to different messages with emotes in the server based on configuration
 * @author etsubu
 */
@Component
public class MessageReacter {
    private static final Logger log = LoggerFactory.getLogger(MessageReacter.class);
    private final ConfigLoader configLoader;

    public MessageReacter(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    /**
     * Checks if the sent message should reacted to
     * @param event Event that was received on server
     * @return Emote that should be used to react to the message
     */
    public Optional<Emote> react(MessageReceivedEvent event) {
        // No reactions to DMs
        if(event.getChannelType() != ChannelType.TEXT) {
            return Optional.empty();
        }
        String serverName = event.getGuild().getId();
        Optional<ServerConfig> serverConfig = configLoader.getConfig().getServerConfig(serverName);
        if(serverConfig.isEmpty()) {
            log.info("No configs for server {}, block by default", serverName);
            return Optional.empty();
        }
        List<Reaction> reactions = serverConfig.get().getReactions();
        if(reactions != null) {
            for (Reaction reaction : reactions) {
                if (reaction.getPattern().matcher(event.getMessage().getContentDisplay().toLowerCase()).matches()) {
                    log.info("Matched message {} with emote {}", event.getMessage().getContentDisplay(), reaction.getReact());
                    List<Emote> emotes = event.getGuild().getEmotesByName(reaction.getReact(), true);
                    if (emotes.size() >= 1) {
                        return Optional.of(emotes.get(0));
                    }
                }
            }
        }
        return Optional.empty();
    }
}

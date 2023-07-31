package com.etsubu.stonksbot.discord;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.Reaction;
import com.etsubu.stonksbot.configuration.ServerConfig;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Reacts to different messages with emotes in the server based on configuration
 *
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
     *
     * @param event Event that was received on server
     * @return Emote that should be used to react to the message
     */
    public Optional<RichCustomEmoji> react(MessageReceivedEvent event) {
        // No reactions to DMs
        if (!event.isFromGuild() || !event.isFromType(ChannelType.TEXT)) {
            return Optional.empty();
        }
        String serverName = event.getGuild().getId();
        Optional<ServerConfig> serverConfig = configLoader.getConfig().getServerConfig(serverName);
        if (serverConfig.isEmpty()) {
            log.info("No configs for server {}, block by default", serverName);
            return Optional.empty();
        }
        List<Reaction> reactions = serverConfig.get().getReactions();
        if (reactions != null) {
            for (Reaction reaction : reactions) {
                if (reaction.getPattern().matcher(event.getMessage().getContentDisplay().toLowerCase()).matches()) {
                    log.info("Matched message {} with emote {}", event.getMessage().getContentDisplay(), reaction.getReact());
                    List<RichCustomEmoji> emotes = event.getGuild().getEmojisByName(reaction.getReact(), true);
                    return emotes.stream().findFirst();
                }
            }
        }
        return Optional.empty();
    }
}

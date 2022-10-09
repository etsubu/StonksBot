package com.etsubu.stonksbot.permission;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ServerConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PermissionManager {
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    private final ConfigLoader configLoader;

    public PermissionManager(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        log.info("Initialized PermissionManager");
    }

    public boolean isReplyAllowed(MessageReceivedEvent event) {
        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();
        boolean isGlobalAdmin = Optional.ofNullable(configLoader.getConfig().getGlobalAdmins())
                .map(x -> x.stream().anyMatch(y -> y.trim().equalsIgnoreCase(user.getId()))).orElse(false);
        if (isGlobalAdmin) {
            return true;
        }
        if (event.getChannelType() != ChannelType.TEXT) {
            return false;
        }
        Optional<List<Role>> userRoles = Optional.ofNullable(event.getMember()).map(Member::getRoles);
        Optional<ServerConfig> serverConfig = configLoader.getConfig().getServerConfig(event.getGuild().getId());
        if (serverConfig.isEmpty()) {
            log.info("No configs for server {}, block by default", event.getGuild().getId());
            return false;
        }
        boolean isInServerAdminGroup = userRoles.map(x -> x.stream()
                .anyMatch(y -> y.getId().trim().equalsIgnoreCase(serverConfig.get().getAdminGroup())))
                .orElse(false);
        log.debug("Is admin: {}", isInServerAdminGroup);
        if (isInServerAdminGroup) {
            return true;
        }
        // If no special permissions then check if the channel is allowed
        // Allow if no channel is whitelisted
        return Optional.ofNullable(serverConfig.get().getWhitelistedChannels())
                .map(x -> x.stream().anyMatch(y -> y.trim().equalsIgnoreCase(channel.getId()))).orElse(false);
    }
}

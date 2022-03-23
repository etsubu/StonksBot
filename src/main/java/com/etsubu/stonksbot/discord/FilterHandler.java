package com.etsubu.stonksbot.discord;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ServerConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FilterHandler {
    private static final Logger log = LoggerFactory.getLogger(FilterHandler.class);
    private final ConfigLoader configLoader;

    public FilterHandler(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    public boolean shouldBeFiltered(Message message, Member member, User author, GenericMessageEvent event) {
        // No reactions to DMs
        if (event.getChannelType() != ChannelType.TEXT || member == null) {
            return false;
        }
        Config config = configLoader.getConfig();
        String serverName = event.getGuild().getId();
        Optional<ServerConfig> serverConfig = config.getServerConfig(serverName);
        if (serverConfig.isEmpty()) {
            return false;
        }
        if (config.getGlobalAdmins().stream().anyMatch(x -> x.equalsIgnoreCase(author.getId()))) {
            // Ignore global admins
            return false;
        }
        // Ignore trusted groups
        if(Optional.ofNullable(serverConfig.get().getTrustedGroup())
                .map(x -> member.getRoles().stream().anyMatch(y -> y.getId().equals(x)))
                .orElse(false)) {
            return false;
        }
        // Ignore admin group
        if(Optional.ofNullable(serverConfig.get().getAdminGroup())
                .map(x -> member.getRoles().stream().anyMatch(y -> y.getId().equals(x)))
                .orElse(false)) {
            return false;
        }
        if (serverConfig.get().getFilters().getRegexPatterns()
                .stream().anyMatch(x -> x.matcher(message.getContentDisplay().toLowerCase()).matches())) {
            // Filter pattern matches
            log.info("Filtering message '{}' by '{}' id='{}'",
                    message.getContentDisplay().replaceAll("\n", ""),
                    author.getName(),
                    author.getId());
            if (serverConfig.get().getFilters().getNotifyChannel() != null) {
                GuildChannel guildChannel = event.getJDA().getGuildChannelById(serverConfig.get().getFilters().getNotifyChannel());
                if (guildChannel instanceof TextChannel) {
                    TextChannel channel = (TextChannel) guildChannel;
                    // Remove the message
                    message.delete().queue();
                    // Send notification to admin channel
                    channel.sendMessage(String.format("Filtered message:%n```%s```%nnUser: %s, id=%s, channel=%s",
                            message.getContentDisplay().replaceAll("`", ""),
                            author.getName(),
                            author.getId(),
                            event.getChannel().getName())).queue();
                    log.info("Filtered message");
                }
            } else {
                log.info("Filtering message but no notification channel exists");
            }
            return true;
        }
        return false;
    }
}

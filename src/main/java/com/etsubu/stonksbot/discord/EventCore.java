package com.etsubu.stonksbot.discord;

import com.etsubu.stonksbot.command.CommandResult;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.AttachmentFile;
import com.etsubu.stonksbot.command.CommandHandler;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.permission.PermissionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;

/**
 * @author etsubu
 * @version 26 Jul 2018
 *
 */
@Component
public class EventCore extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(EventCore.class);
    private final CommandHandler commandHandler;
    private JDA jda;
    private final PermissionManager permissionManager;
    private final MessageReacter reacter;
    private final FilterHandler filterHandler;
    private final ConfigLoader configLoader;

    /**
     * Initializes EventCore
     */
    public EventCore(CommandHandler commandHandler,
                     ConfigLoader configLoader,
                     PermissionManager permissionManager,
                     MessageReacter reacter,
                     FilterHandler filterHandler) throws LoginException {
        this.configLoader = configLoader;
        this.commandHandler = commandHandler;
        this.permissionManager = permissionManager;
        this.reacter = reacter;
        this.filterHandler = filterHandler;
        new Thread(this::start).start();
    }

    public boolean start() {
        if(jda != null) {
            jda.shutdown();
        }
        Config config = configLoader.getConfig();
        Optional<String> oauth = Optional.ofNullable(config.getOauth());
        if(oauth.isEmpty()) {
            log.error("No oath token present");
            return false;
        }
        try {
            this.jda = JDABuilder.createDefault(oauth.get()).build();
        } catch (LoginException e) {
            log.error("Login failed, check the network connection and oath token");
            return false;
        }
        jda.addEventListener(this);
        return true;
    }

    /**
     * Sends text message to the requested channel on server
     * @param channelIds
     * @param message
     * @return
     */
    public boolean sendMessage(List<Long> channelIds, String message, List<AttachmentFile> attachmentFiles) {
        boolean sent = false;
        for(long channelId : channelIds) {
            GuildChannel guildChannel = jda.getGuildChannelById(channelId);
            if(guildChannel instanceof TextChannel) {
                MessageAction msg = ((TextChannel)guildChannel).sendMessage(message);
                if(attachmentFiles != null && !attachmentFiles.isEmpty()) {
                    for(AttachmentFile file : attachmentFiles) {
                        msg = msg.addFile(file.getFile(), file.getFilename());
                    }
                }
                msg.queue();
                sent = true;
            } else {
                log.warn("Failed to find text channel with id {}", channelId);
            }
        }
        return sent;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot() || event.getAuthor().getName().equalsIgnoreCase(jda.getSelfUser().getName())) {
            // Skip messages sent by bots or ourselves
            return;
        }
        reacter.react(event).ifPresent(x -> event.getMessage().addReaction(x).queue());
        if(filterHandler.shouldBeFiltered(event)) {
            return;
        }

        if (this.commandHandler.isCommand(event.getMessage().getContentDisplay()) && event.getMessage().getContentDisplay().length() > 1) {
            if(!permissionManager.isReplyAllowed(event)) {
                log.info("Prevented reply for {}  on channel {}", event.getAuthor().getName(), event.getChannel().getName());
                event.getChannel().sendMessage("Please use whitelisted channel for performing commands").queue();
                return;
            }
            try {
                CommandResult result = commandHandler.execute(event);
                event.getChannel().sendMessage(result.getResponse()).queue();
                if(result.getResponse().isEmpty() || result.getResponse().isBlank()) {
                    log.error("Command returned blank response");
                    event.getChannel().sendMessage("Oops, this command returned blank response. Developer should probably take a look at logs.").queue();
                }
                if (result.getSucceeded()) {
                    log.info("Successfully executed user command: {}", event.getMessage().getContentDisplay().replaceAll("\n", ""));
                } else {
                    log.error("Failed to execute user command: " + result.getResponse() + " - "
                            + Optional.ofNullable(result.getException())
                            .map(Exception::getMessage)
                            .orElse(""));
                }
            } catch (Exception e) {
                log.error("Uncaught exception", e);
                event.getChannel().sendMessage("Oops, this command caused unexpected exception. Developer should probably take a look at logs.").queue();
            }
        }
    }
}
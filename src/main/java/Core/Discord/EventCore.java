package Core.Discord;

import Core.Commands.CommandHandler;
import Core.Commands.CommandResult;
import Core.Configuration.ConfigLoader;
import Core.Permissions.PermissionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;

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
    private final JDA jda;
    private final PermissionManager permissionManager;
    private final MessageReacter reacter;

    /**
     * Initializes EventCore
     */
    public EventCore(CommandHandler commandHandler, ConfigLoader configLoader, PermissionManager permissionManager, MessageReacter reacter) throws LoginException {
        this.jda = JDABuilder.createDefault(configLoader.getConfig().getOauth()).build();
        jda.addEventListener(this);
        this.commandHandler = commandHandler;
        this.permissionManager = permissionManager;
        this.reacter = reacter;
        log.info("Discord name: {}", jda.getSelfUser().getName());
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot() || event.getAuthor().getName().equalsIgnoreCase(jda.getSelfUser().getName())) {
            // Skip messages sent by bots or ourselves
            return;
        }
        reacter.react(event).ifPresent(x -> event.getMessage().addReaction(x).queue());

        if (this.commandHandler.isCommand(event.getMessage().getContentDisplay())) {
            if(!permissionManager.isReplyAllowed(event)) {
                log.info("Prevented reply for {}  on channel {}", event.getAuthor().getName(), event.getChannel().getName());
                event.getChannel().sendMessage("Please use whitelisted channel for performing commands").queue();
                return;
            }
        	CommandResult result = commandHandler.execute(event.getMessage().getContentDisplay());
            event.getChannel().sendMessage(result.getResponse()).queue();
        	if(result.getSucceeded()) {
        	    log.info("Successfully executed user command: {}", event.getMessage().getContentDisplay().replaceAll("\n", ""));
            } else {
        	    log.error("Failed to execute user command: " + result.getResponse() + " - " +
                        ((result.getException() != null) ? result.getException().getMessage() : ""));
            }
        }
    }
}
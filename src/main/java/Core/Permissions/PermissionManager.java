package Core.Permissions;

import Core.Configuration.ConfigLoader;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
        boolean isGlobalAdmin = Optional.ofNullable(configLoader.getConfig().getAdmins())
                .map(x -> x.stream().anyMatch(y -> y.trim().equalsIgnoreCase(user.getName()))).orElse(false);
        if(isGlobalAdmin) {
            return true;
        }
        if(event.getChannelType() != ChannelType.TEXT) {
            return false;
        }
        // Allow if no channel is whitelisted
        return Optional.ofNullable(configLoader.getConfig().getWhitelistedChannels())
                .map(x -> x.stream().anyMatch(y -> y.trim().equalsIgnoreCase(channel.getName()))).orElse(true);
    }
}

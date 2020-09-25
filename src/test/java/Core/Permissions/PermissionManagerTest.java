package Core.Permissions;

import Core.Configuration.Config;
import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Runs unit tests for permission manager
 * @author etsubu
 */
@ExtendWith(MockitoExtension.class)
public class PermissionManagerTest {
    private static final String NAME = "nagrodus";
    private static final String GUILD_NAME = "test_guild";
    private static final String TRUSTED_ROLE = "trusted_role";
    private static final String ADMIN_ROLE = "admin_role";
    private static final String WHITELISTED_CHANNEL = "whitelisted_channel";
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private MessageReceivedEvent event;
    @Mock
    private User user;
    @Mock
    private Guild guild;
    @Mock
    private Member member;
    @Mock
    private Role role;
    @Mock
    private MessageChannel channel;
    private Config config;
    private PermissionManager permissionManager;

    @BeforeEach
    public void init() {
        config = new Config();
        when(configLoader.getConfig()).thenReturn(config);
        permissionManager = new PermissionManager(configLoader);
    }

    /**
     * Global admins should receive response always, including direct messages
     */
    @Test
    public void testRespondsToGlobalAdmins() {
        when(user.getName()).thenReturn(NAME);
        when(event.getAuthor()).thenReturn(user);
        // Verify that blocked by default
        assertFalse(permissionManager.isReplyAllowed(event));
        config.setGlobalAdmins(List.of("asdasd"));
        // Should not respond if sender is not admin
        assertFalse(permissionManager.isReplyAllowed(event));
        config.setGlobalAdmins(List.of("asdasd", NAME));
        // Sender is global admin, should respond
        assertTrue(permissionManager.isReplyAllowed(event));
    }

    /**
     * Bot shouldn't respond to direct messages if those aren't from global admin
     */
    @Test
    public void testDoesNotRespondToDirectMessages() {
        when(event.getChannelType()).thenReturn(ChannelType.PRIVATE);
        assertFalse(permissionManager.isReplyAllowed(event));
    }

    /**
     * Bot shouldn't respond if no configurations exist for the given server
     */
    @Test
    public void testDoesNotRespondForEmptyServerConfig() {
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(event.getGuild()).thenReturn(guild);
        when(event.getAuthor()).thenReturn(user);
        assertFalse(permissionManager.isReplyAllowed(event));
    }

    /**
     * Server admins should receive a response, no matter what channel
     */
    @Test
    public void testRespondsToServerAdmins() {
        // Create server config
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(GUILD_NAME);
        serverConfig.setAdminGroup(ADMIN_ROLE);
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(role.getName()).thenReturn(ADMIN_ROLE);
        when(member.getRoles()).thenReturn(List.of(role));
        when(event.getMember()).thenReturn(member);
        when(event.getGuild()).thenReturn(guild);
        assertTrue(permissionManager.isReplyAllowed(event));
    }

    /**
     * Trusted group should receive a response, no matter what channel
     */
    @Test
    public void testRespondsToServerTrustedUsers() {
        // Create server config
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(GUILD_NAME);
        serverConfig.setTrustedGroup(TRUSTED_ROLE);
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(role.getName()).thenReturn(TRUSTED_ROLE);
        when(member.getRoles()).thenReturn(List.of(role));
        when(event.getMember()).thenReturn(member);
        when(event.getGuild()).thenReturn(guild);
        assertTrue(permissionManager.isReplyAllowed(event));
    }

    /**
     * All users should receive a response if they are using whitelisted channel
     */
    @Test
    public void testRespondsOnWhitelistedChannel() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(GUILD_NAME);
        serverConfig.setWhitelistedChannels(List.of(WHITELISTED_CHANNEL));
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(channel.getName()).thenReturn(WHITELISTED_CHANNEL);
        when(event.getChannel()).thenReturn(channel);
        when(event.getGuild()).thenReturn(guild);
        assertTrue(permissionManager.isReplyAllowed(event));
    }

    /**
     * No response by default if the channel is not whitelisted
     */
    @Test
    public void testDoesNotRespondOnNonWhitelistedChannel() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(GUILD_NAME);
        serverConfig.setWhitelistedChannels(List.of(WHITELISTED_CHANNEL));
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(channel.getName()).thenReturn("some_general_channel");
        when(event.getChannel()).thenReturn(channel);
        when(event.getGuild()).thenReturn(guild);
        assertFalse(permissionManager.isReplyAllowed(event));
    }
}

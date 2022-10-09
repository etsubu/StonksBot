package com.etsubu.stonksbot.permission;

import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ServerConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Runs unit tests for permission manager
 * @author etsubu
 */
@ExtendWith(MockitoExtension.class)
public class PermissionManagerTest {
    private static final String NAME_ID = "123456";
    private static final String GUILD_ID= "1234567";
    private static final String ADMIN_ROLE = "12";
    private static final String WHITELISTED_CHANNEL = "12345";
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
    private MessageChannelUnion channel;
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
        when(user.getId()).thenReturn(NAME_ID);
        when(event.getAuthor()).thenReturn(user);
        // Verify that blocked by default
        assertFalse(permissionManager.isReplyAllowed(event));
        config.setGlobalAdmins(List.of("000"));
        // Should not respond if sender is not admin
        assertFalse(permissionManager.isReplyAllowed(event));
        config.setGlobalAdmins(List.of("000", NAME_ID));
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
        when(guild.getId()).thenReturn(GUILD_ID);
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
        serverConfig.setName(GUILD_ID);
        serverConfig.setAdminGroup(ADMIN_ROLE);
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getId()).thenReturn(GUILD_ID);
        when(role.getId()).thenReturn(ADMIN_ROLE);
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
        serverConfig.setName(GUILD_ID);
        serverConfig.setWhitelistedChannels(List.of(WHITELISTED_CHANNEL));
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getId()).thenReturn(GUILD_ID);
        when(channel.getId()).thenReturn(WHITELISTED_CHANNEL);
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
        serverConfig.setName(GUILD_ID);
        serverConfig.setWhitelistedChannels(List.of(WHITELISTED_CHANNEL));
        config.setServers(List.of(serverConfig));
        when(event.getChannelType()).thenReturn(ChannelType.TEXT);
        when(guild.getId()).thenReturn(GUILD_ID);
        when(channel.getId()).thenReturn("404");
        when(event.getChannel()).thenReturn(channel);
        when(event.getGuild()).thenReturn(guild);
        assertFalse(permissionManager.isReplyAllowed(event));
    }
}

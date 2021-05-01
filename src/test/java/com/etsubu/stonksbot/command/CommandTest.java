package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.CommandConfig;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ServerConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommandTest {
    private static final String USER_ID = "1234";
    private static final String SERVER_ID = "123456";
    @Mock
    private Config config;
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private ServerConfig serverConfig;
    @Mock
    private Role role;
    @Mock
    private User user;
    @Mock
    private Guild guild;

    public static class TestCommand extends Command {
        public TestCommand(ConfigLoader configLoader, boolean allowByDefault) {
            super("name", configLoader, allowByDefault);
        }

        @Override
        public CommandResult exec(String command) { return null; }
        @Override
        public String help() { return null; }
    }

    @BeforeEach
    public void init() {
        when(user.getId()).thenReturn(USER_ID);
        when(guild.getId()).thenReturn(SERVER_ID);
        when(configLoader.getConfig()).thenReturn(config);
    }

    @Test
    public void testAllowGlobalAdmins() {
        when(config.getGlobalAdmins()).thenReturn(List.of(USER_ID));
        TestCommand test = new TestCommand(configLoader, false);
        assertTrue(test.isUserAllowed(user, guild, List.of(role)));

        verify(configLoader, times(1)).getConfig();
    }

    @Test
    public void testNullServerName() {
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(user, null, List.of(role)));
        assertTrue(test2.isUserAllowed(user, null, List.of(role)));

        verify(configLoader, times(2)).getConfig();
    }

    @Test
    public void testUnknownServer() {
        when(config.getServerConfig(anyString())).thenReturn(Optional.empty());
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(user, guild, List.of(role)));
        assertTrue(test2.isUserAllowed(user, guild, List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig(SERVER_ID);
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testNoCommandConfigs() {
        when(config.getServerConfig(anyString())).thenReturn(Optional.of(serverConfig));
        when(serverConfig.getCommands()).thenReturn(new HashMap<>());
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(user, guild, List.of(role)));
        assertTrue(test2.isUserAllowed(user, guild, List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig(SERVER_ID);
        verify(serverConfig, times(2)).getCommands();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testCommandNotAllowed() {
        CommandConfig commandConfig = new CommandConfig();
        commandConfig.setAllowedGroups(List.of("some_group"));
        when(role.getName()).thenReturn("default");
        when(config.getServerConfig(anyString())).thenReturn(Optional.of(serverConfig));
        when(serverConfig.getCommands()).thenReturn(Map.of("name", commandConfig));
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(user, guild, List.of(role)));
        assertTrue(test2.isUserAllowed(user, guild, List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig(SERVER_ID);
        verify(serverConfig, times(2)).getCommands();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testCommandAllowed() {
        CommandConfig commandConfig = new CommandConfig();
        commandConfig.setAllowedGroups(List.of("default"));
        when(role.getId()).thenReturn("default");
        when(config.getServerConfig(anyString())).thenReturn(Optional.of(serverConfig));
        when(serverConfig.getCommands()).thenReturn(Map.of("name", commandConfig));
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        assertTrue(test.isUserAllowed(user, guild, List.of(role)));
        verify(configLoader, times(1)).getConfig();
        verify(config, times(1)).getServerConfig(SERVER_ID);
        verify(serverConfig, times(1)).getCommands();
        verify(role, times(1)).getId();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testNoRoles() {
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(user, guild, null));
        assertFalse(test.isUserAllowed(user, guild, new LinkedList<>()));
        assertTrue(test2.isUserAllowed(user, guild, null));
        assertTrue(test2.isUserAllowed(user, guild, new LinkedList<>()));

        verify(configLoader, times(4)).getConfig();
        verify(config, times(4)).getGlobalAdmins();
    }
}

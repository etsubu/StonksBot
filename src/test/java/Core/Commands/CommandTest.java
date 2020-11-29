package Core.Commands;

import Core.Configuration.CommandConfig;
import Core.Configuration.Config;
import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandTest {
    private static final String USERNAME = "test_user";
    private static final String SERVER_NAME = "server_name";
    @Mock
    private Config config;
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private ServerConfig serverConfig;
    @Mock
    private Role role;

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
        when(configLoader.getConfig()).thenReturn(config);
    }

    @Test
    public void testAllowGlobalAdmins() {
        //        when(config.getServerConfig(SERVER_NAME)).thenReturn(Optional.of(serverConfig));
        when(config.getGlobalAdmins()).thenReturn(List.of(USERNAME));
        TestCommand test = new TestCommand(configLoader, false);
        assertTrue(test.isUserAllowed(USERNAME, SERVER_NAME, List.of(role)));

        verify(configLoader, times(1)).getConfig();
    }

    @Test
    public void testNullServerName() {
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(USERNAME, null, List.of(role)));
        assertTrue(test2.isUserAllowed(USERNAME, null, List.of(role)));

        verify(configLoader, times(2)).getConfig();
    }

    @Test
    public void testUnknownServer() {
        when(config.getServerConfig(anyString())).thenReturn(Optional.empty());
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(USERNAME, "asd", List.of(role)));
        assertTrue(test2.isUserAllowed(USERNAME, "asd", List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig("asd");
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testNoCommandConfigs() {
        when(config.getServerConfig(anyString())).thenReturn(Optional.of(serverConfig));
        when(serverConfig.getCommands()).thenReturn(new HashMap<>());
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(USERNAME, "asd", List.of(role)));
        assertTrue(test2.isUserAllowed(USERNAME, "asd", List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig("asd");
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
        assertFalse(test.isUserAllowed(USERNAME, "asd", List.of(role)));
        assertTrue(test2.isUserAllowed(USERNAME, "asd", List.of(role)));

        verify(configLoader, times(2)).getConfig();
        verify(config, times(2)).getServerConfig("asd");
        verify(serverConfig, times(2)).getCommands();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testCommandAllowed() {
        CommandConfig commandConfig = new CommandConfig();
        commandConfig.setAllowedGroups(List.of("default"));
        when(role.getName()).thenReturn("default");
        when(config.getServerConfig(anyString())).thenReturn(Optional.of(serverConfig));
        when(serverConfig.getCommands()).thenReturn(Map.of("name", commandConfig));
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        assertTrue(test.isUserAllowed(USERNAME, "asd", List.of(role)));
        verify(configLoader, times(1)).getConfig();
        verify(config, times(1)).getServerConfig("asd");
        verify(serverConfig, times(1)).getCommands();
        verify(role, times(1)).getName();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @Test
    public void testNoRoles() {
        when(config.getGlobalAdmins()).thenReturn(new LinkedList<>());
        TestCommand test = new TestCommand(configLoader, false);
        TestCommand test2 = new TestCommand(configLoader, true);
        assertFalse(test.isUserAllowed(USERNAME, "asd", null));
        assertFalse(test.isUserAllowed(USERNAME, "asd", new LinkedList<>()));
        assertTrue(test2.isUserAllowed(USERNAME, "asd", null));
        assertTrue(test2.isUserAllowed(USERNAME, "asd", new LinkedList<>()));

        verify(configLoader, times(4)).getConfig();
        verify(config, times(4)).getGlobalAdmins();
    }
}

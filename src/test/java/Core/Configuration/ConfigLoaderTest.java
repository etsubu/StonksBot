package Core.Configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that yaml configurations are loaded and parsed properly
 */
public class ConfigLoaderTest {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoaderTest.class);
    private ConfigLoader configLoader;

    @BeforeEach
    public void init() {
        configLoader = new ConfigLoader(Paths.get("./src/test/resources/config.yaml"));
    }

    @Test
    public void testConfigurationLoading() {
        log.info("Running config loader tests");
        Config config = configLoader.getConfig();
        assertNotNull(config);
        assertNotNull(config.getServers());
        assertNotNull(config.getGlobalAdmins());
        assertEquals("test_oauth", config.getOauth());
        assertEquals(1, config.getGlobalAdmins().size());
        assertEquals("nagrodus", config.getGlobalAdmins().get(0));
        List<ServerConfig> serverConfigList = config.getServers();
        assertEquals(1, serverConfigList.size());
        Optional<ServerConfig> testServer = serverConfigList.stream().filter(x -> "test_server".equals(x.getName())).findFirst();
        assertTrue(testServer.isPresent());
        // Verify server configurations
        assertEquals("some_admin_group", testServer.get().getAdminGroup());
        assertEquals("some_trusted_group", testServer.get().getTrustedGroup());
        assertNotNull(testServer.get().getWhitelistedChannels());
        assertNotNull(testServer.get().getReactions());
        assertEquals(1, testServer.get().getReactions().size());
        assertEquals(1, testServer.get().getWhitelistedChannels().size());
        assertEquals("whitelisted_channel", testServer.get().getWhitelistedChannels().get(0));
        Reaction r = testServer.get().getReactions().get(0);
        assertEquals(".*test.*", r.getMessage());
        assertEquals("testReaction", r.getReact());
        // Verify command permissions
        assertNotNull(serverConfigList.get(0).getCommands());
        assertTrue(serverConfigList.get(0).getCommands().containsKey("fscore"));
        CommandConfig fscoreConfig = serverConfigList.get(0).getCommands().get("fscore");
        assertNotNull(fscoreConfig.getAllowedGroups());
        assertEquals(2, fscoreConfig.getAllowedGroups().size());
        assertTrue(fscoreConfig.getAllowedGroups().contains("test_group"));
        assertTrue(fscoreConfig.getAllowedGroups().contains("test_group2"));
    }
}

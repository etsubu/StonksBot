package Core.Configuration;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

@Component
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private Config config;
    private final String configFileName = "config.yaml";
    private Instant lastModified;

    public ConfigLoader() {
        loadConfigs();
    }

    private void loadConfigs() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        try {
            config = yaml.load(Files.readString(Paths.get("config.yaml")));
            log.info("Configs loaded");
        } catch (IOException e) {
            log.error("Failed to load configuration");
            throw new RuntimeException("Could not load configuration file");
        }
        lastModified = Instant.now();
    }

    public Config getConfig() {
        try {
            FileTime time = Files.getLastModifiedTime(Paths.get(configFileName));
            if(time.toInstant().isAfter(lastModified)) {
                log.info("Changes in config files, reloading.");
                loadConfigs();
            }
        } catch (IOException e) {
            log.error("Failed to get last modified time for config file. Assuming it has not changed", e);
        }
        return config;
    }
}

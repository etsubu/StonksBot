package Core.Configuration;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private Config config;
    private Path configFile;
    private Instant lastModified;

    public ConfigLoader() {
        this(Paths.get("config.yaml"));
    }

    public ConfigLoader(Path path) {
        this.configFile = path;
        loadConfigs();
    }

    private void loadConfigs() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        try {
            config = yaml.load(Files.readString(configFile));
            log.info("Configs loaded");
        } catch (IOException e) {
            log.error("Failed to load configuration");
            throw new RuntimeException("Could not load configuration file");
        }
        lastModified = Instant.now();
        config.getServers().forEach(x -> Optional.ofNullable(x.getReactions()).ifPresent(y -> y.forEach(Reaction::buildPattern)));
    }

    public Config getConfig() {
        try {
            FileTime time = Files.getLastModifiedTime(configFile);
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

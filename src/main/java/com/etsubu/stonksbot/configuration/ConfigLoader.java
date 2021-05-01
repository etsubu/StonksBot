package com.etsubu.stonksbot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

@Component
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Config DEFAULT_CONFIG = new Config();
    private Config config;
    private final Path configFile;
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
            // Compile all regex patterns
            config.getServers().forEach(y -> {
                y.getReactions().forEach(Reaction::buildPattern);
                y.getFilters().update();
            });
        } catch (IOException e) {
            log.error("Failed to load configuration");
        }
        lastModified = Instant.now();
    }

    public Config getConfig() {
        try {
            FileTime time = Files.getLastModifiedTime(configFile);
            if(time.toInstant().isAfter(lastModified)) {
                log.info("Changes in config files, reloading.");
                loadConfigs();
            }
        } catch (NoSuchFileException e) {
            log.error("No config file present");
            return new Config();
        }
        catch (IOException e) {
            log.error("Failed to get last modified time for config file. Assuming it has not changed", e);
        }
        return Optional.ofNullable(config).orElse(DEFAULT_CONFIG);
    }
}

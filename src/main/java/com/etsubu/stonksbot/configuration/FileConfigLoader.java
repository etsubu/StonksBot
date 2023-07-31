package com.etsubu.stonksbot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileConfigLoader implements IConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(FileConfigLoader.class);
    private Instant lastModified = Instant.EPOCH;
    private final Path configFile;
    private final Yaml yaml;
    private Config config;

    public FileConfigLoader(Path configFile) {
        this.configFile = configFile;
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
    }
    @Override
    public Config loadConfig() {
        try {
            FileTime time = Files.getLastModifiedTime(configFile);
            if (time.toInstant().isBefore(lastModified)) {
                return config;
            }
        } catch (IOException e) {
            log.error("Failed to check last modified time", e);
        }
        log.info("Changes in config files, reloading.");
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
        return config;
    }
}

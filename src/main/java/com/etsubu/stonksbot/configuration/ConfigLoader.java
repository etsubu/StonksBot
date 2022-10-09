package com.etsubu.stonksbot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Config DEFAULT_CONFIG = new Config();
    private final Path configFile;
    private final IConfigLoader configLoaderImpl;
    private boolean testFlag;

    public ConfigLoader() {
        this(Path.of(Optional.ofNullable(System.getProperty("STONKSBOT_CONFIG_FILE")).orElse("config.yaml")));
    }

    public ConfigLoader(Path path) {
        testFlag = Optional.ofNullable(System.getProperty("environment")).map(x -> x.equals("test")).orElse(false);
        this.configFile = path;
        if(testFlag) {
            log.info("Enabled test mode");
            configLoaderImpl = null;
            return;
        }
        var awsConfig = Path.of("aws-config.yaml");
        if(!Files.exists(path) && Files.exists(awsConfig)) {
            log.info("Attempting to load configs from AWS");
            var s3 = new Yaml(new Constructor(S3Config.class));
            try {
                configLoaderImpl = new S3ConfigLoader(s3.load(Files.readString(awsConfig)));
            } catch (IOException e) {
                throw new RuntimeException("AWS configuration file could not be loaded");
            }
        } else {
            configLoaderImpl = new FileConfigLoader(configFile);
        }
        getConfig();
    }

    public Config getConfig() {
        if(testFlag) {
            return new Config();
        }
        return Optional.ofNullable(configLoaderImpl.loadConfig()).orElse(DEFAULT_CONFIG);
    }
}

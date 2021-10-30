package com.etsubu.stonksbot.configuration;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalSync implements ConfigurationSync {
    private static final Logger log = LoggerFactory.getLogger(LocalSync.class);
    private final Path path = Paths.get("configs");
    private final Gson gson;

    public LocalSync() {
        if(!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                log.error("Failed to create directory for local configs");
            }
        }
        gson = new Gson();
    }
    @Override
    public <T> Optional<T> loadConfiguration(String key, Class<T> c) {
        Path configPath = path.resolve(key);
        if(!Files.exists(configPath)) {
            return Optional.empty();
        }
        try {
            log.info("Loading configuration with key='{}'", key);
            return Optional.of(gson.fromJson(Files.readString(configPath, StandardCharsets.UTF_8), c));
        } catch (Exception e) {
            log.error("Failed to read config path {}", configPath.toAbsolutePath(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean saveConfiguration(String key, Object content) {
        Path configPath = path.resolve(key);
        try {
            Files.writeString(configPath, gson.toJson(content));
            log.info("Saved config with key='{}'", key);
            return true;
        } catch (IOException e) {
            log.error("Failed to save config to path {}", configPath.toAbsolutePath(), e);
            return false;
        }
    }
}

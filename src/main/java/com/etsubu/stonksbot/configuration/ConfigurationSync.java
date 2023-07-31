package com.etsubu.stonksbot.configuration;

import java.util.Optional;

public interface ConfigurationSync {
    <T> Optional<T> loadConfiguration(String key, Class<T> c);
    boolean saveConfiguration(String key, Object content);
}

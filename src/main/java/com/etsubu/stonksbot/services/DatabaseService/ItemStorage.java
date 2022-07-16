package com.etsubu.stonksbot.services.DatabaseService;

import java.util.Map;
import java.util.Optional;

public interface ItemStorage {
    int entries(String serverId);
    boolean addEntry(String serverId, String key, Map<String, String> values);
    boolean removeEntry(String serverId, String key);
    Optional<Map<String, String>> getEntry(String serverId, String key);
}

package com.etsubu.stonksbot.configuration;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Setter
@NoArgsConstructor
public class Config {
    private String oauth;
    private S3Config s3;
    private FeatureConfig omxhNews;
    private FeatureConfig shareville;
    private FeatureConfig inderes;
    private List<String> globalAdmins;
    private List<ServerConfig> servers;

    public Optional<ServerConfig> getServerConfig(String id) {
        if (servers == null) {
            return Optional.empty();
        }
        return servers.stream().filter(x -> x.getName().trim().equalsIgnoreCase(id)).findFirst();
    }

    public String getOauth() {
        return Optional.ofNullable(oauth).orElseGet(() -> System.getProperty("STONKSBOT_OATH"));
    }

    public FeatureConfig getOmxhNews() {
        return Optional.ofNullable(omxhNews).orElseGet(() -> new FeatureConfig("false"));
    }

    public List<String> getGlobalAdmins() {
        return Optional.ofNullable(globalAdmins).orElseGet(LinkedList::new);
    }

    public List<ServerConfig> getServers() {
        return Optional.ofNullable(servers).orElseGet(LinkedList::new);
    }

    public FeatureConfig getShareville() {
        return Optional.ofNullable(shareville).orElseGet(() -> new FeatureConfig("false"));
    }

    public FeatureConfig getInderes() {
        return Optional.ofNullable(inderes).orElseGet(() -> new FeatureConfig("false"));
    }

    public S3Config getS3() {
        return s3;
    }
}

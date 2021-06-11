package com.etsubu.stonksbot.configuration;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Setter
@NoArgsConstructor
public class ServerConfig {
    private String name;
    private List<String> whitelistedChannels;
    private List<Reaction> reactions;
    private FilterConfig filters;
    private String adminGroup;
    private String trustedGroup;
    private Map<String, CommandConfig> commands;
    private Long newsChannel;
    private Long recommendationChannel;
    private SharevilleConfig shareville;
    private String votelinkTemplate;

    public String getName() { return name; }

    public List<String> getWhitelistedChannels() { return Optional.ofNullable(whitelistedChannels).orElseGet(LinkedList::new); }

    public List<Reaction> getReactions() { return Optional.ofNullable(reactions).orElseGet(LinkedList::new); }

    public FilterConfig getFilters() { return Optional.ofNullable(filters).orElseGet(FilterConfig::new); }

    public String getAdminGroup() { return adminGroup; }

    public String getTrustedGroup() { return trustedGroup; }

    public Map<String, CommandConfig> getCommands() { return Optional.ofNullable(commands).orElseGet(HashMap::new); }

    public Long getNewsChannel() { return newsChannel; }

    public Long getRecommendationChannel() { return recommendationChannel; }

    public SharevilleConfig getShareville() { return Optional.ofNullable(shareville).orElseGet(SharevilleConfig::new); }

    public String getVotelinkTemplate() { return votelinkTemplate; }

}

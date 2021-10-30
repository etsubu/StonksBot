package com.etsubu.stonksbot.scheduler.shareville;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.scheduler.shareville.Model.SharevilleUser;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.scheduler.shareville.Model.WallEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduled service that will track a list of shareville users and sends notifications when new transaction happen to
 * a discord channel
 */
@Component
@EnableAsync
public class SharevilleTracker {
    private static final Logger log = LoggerFactory.getLogger(SharevilleTracker.class);
    private static final String PROFILE_URL_TEMPLATE = "https://www.shareville.fi/api/v1/profiles/%s/stream";
    private static final int DELAY = 300; // 5min
    private final ConfigLoader configLoader;
    private final Map<String, SharevilleUser> profileMap;
    private final EventCore eventCore;

    public SharevilleTracker(ConfigLoader configLoader, EventCore eventCore) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        profileMap = new HashMap<>();
    }

    private String buildNotification(List<SharevilleUser> users) {
        StringBuilder builder = new StringBuilder();
        boolean content = false;
        for (SharevilleUser user : users) {
            SharevilleUser old = profileMap.get(user.getUrl());
            if (old.getLatestTranscationDate().isPresent()) {
                List<WallEntry> transactions = user.getTransactionAfter(old.getLatestTranscationDate().get());
                if (transactions != null && transactions.size() > 0) {
                    for (WallEntry we : transactions) {
                        if (we.isValid()) {
                            content = true;
                            builder.append("```\nShareville tapahtumat:");
                            String name = we.getFirst().getProfile().getName();
                            builder.append("\nKäyttäjä: ").append(name).append('\n');
                            builder.append("Kohde: ").append(we.getTransaction().getInstrument().getName()).append('\n');
                            builder.append("Tyyppi: ").append(we.getTransaction().getSideAsDescriptive()).append('\n');
                            builder.append("Hinta: ").append(we.getTransaction().getPrice()).append(we.getTransaction().getInstrument().getCurrency()).append('\n');
                            builder.append("---------------```\n");
                        }
                    }
                }
            }
        }
        return content ? builder.toString() : null;
    }

    public void notify(List<ServerConfig> serverConfigs, List<SharevilleUser> sharevilleUsers) {
        for (ServerConfig server : serverConfigs) {
            List<SharevilleUser> changedUsers = new ArrayList<>(sharevilleUsers);
            Long channel = server.getShareville().getSharevilleChannel();
            // Filter only those that the server is tracking
            changedUsers.removeIf(x -> server.getShareville().getSharevilleProfiles().stream().noneMatch(y -> x.getUrl().contains("/" + y + "/")));
            changedUsers.removeIf(x -> !profileMap.containsKey(x.getUrl()));
            // Filter profiles that have new transactions
            //changedUsers.removeIf(x -> profileMap.containsKey(x.getUrl())
            //        && x.getLatestTransactionTimestamp().compareTo(profileMap.get(x.getUrl()).getLatestTransactionTimestamp()) >= 0);
            // We now have a list of profiles that the server tracks and that have new transactions
            String notification = buildNotification(changedUsers);
            if (notification != null) {
                log.info("Sent shareville transaction notification");
                eventCore.sendMessage(List.of(channel), notification, null);
            }
        }
    }

    @Async
    /* Every other minute during weekdays from 8am-23 Helsinki time */
    @Scheduled(cron = "2 0/2 7-23 ? * MON-FRI", zone = "Europe/Helsinki")
    public void invoke() {
        if (!Boolean.parseBoolean(configLoader.getConfig().getShareville().getEnabled())) {
            // feature is not enabled;
            return;
        }
        // Filter servers that have configs enabled
        List<ServerConfig> serverConfigs = configLoader.getConfig().getServers()
                .stream()
                .filter(x -> x.getShareville().getSharevilleChannel() != null && x.getShareville().getSharevilleProfiles().size() > 0)
                .collect(Collectors.toList());
        // Collect all profile ids that should be followed
        Set<String> profileIds = serverConfigs.stream()
                .map(ServerConfig::getShareville)
                .map(x -> x.getSharevilleProfiles().stream().map(y -> String.format(PROFILE_URL_TEMPLATE, y)).collect(Collectors.toSet()))
                .collect(HashSet::new, Set::addAll, Set::addAll);
        if (profileIds.isEmpty()) {
            // No profiles to track, skip
            return;
        }
        try {
            // Query profile walls from shareville
            Map<String, String> walls = HttpApi.sendMultipleGet(profileIds);
            // Convert to valid profiles
            List<SharevilleUser> wallProfiles = walls.entrySet().stream().map(x -> {
                try {
                    return new SharevilleUser(x.getValue(), x.getKey());
                } catch (IllegalArgumentException e) {
                    log.error("Failed to map profile page ", e);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            // Remove profiles that are not tracked anymore
            profileMap.entrySet().removeIf(x -> !profileIds.contains(x.getKey()));
            // Filter profiles that have new transactions
            notify(serverConfigs, wallProfiles);
            // Update profiles
            wallProfiles.forEach(x -> profileMap.put(x.getUrl(), x));
        } catch (IOException | InterruptedException e) {
            log.error("Connection error while retrieving profile walls", e);
        }
    }
}

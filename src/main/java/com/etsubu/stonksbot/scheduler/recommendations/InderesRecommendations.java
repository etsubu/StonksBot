package com.etsubu.stonksbot.scheduler.recommendations;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ConfigurationSync;
import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.inderes.InderesConnector;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.scheduler.recommendations.model.ChangedRecommendation;
import com.etsubu.stonksbot.scheduler.recommendations.model.NewRecommendation;
import com.etsubu.stonksbot.scheduler.recommendations.model.RecommendationChange;
import com.etsubu.stonksbot.scheduler.recommendations.model.RemovedRecommendation;
import com.etsubu.stonksbot.yahoo.YahooConnector;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class InderesRecommendations {
    private static final Logger log = LoggerFactory.getLogger(InderesRecommendations.class);
    private static final String CACHE_KEY = "inderes";
    // ~1 days freshness
    private static final long FRESHNESS_WINDOW = 1000 * 60 * 60 * 24;
    private static final long CACHE_TTL = 1000 * 60 * 60 * 24 * 2; // 2 day
    // This is used for exponential backoff in case the server fails to respond
    private int failureCounter = 0;
    private int failureTempCounter = 0;
    private final Map<String, RecommendationEntry> entries;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;
    private final InderesConnector inderesConnector;
    private final YahooConnector yahooConnector;
    private final ConfigurationSync configSync;

    public InderesRecommendations(EventCore eventCore, ConfigLoader configLoader,
                                  InderesConnector inderesConnector, YahooConnector yahooConnector,
                                  ConfigurationSync configSync) {
        this.eventCore = eventCore;
        this.configLoader = configLoader;
        this.inderesConnector = inderesConnector;
        this.yahooConnector = yahooConnector;
        this.configSync = configSync;
        entries = new HashMap<>();

        Optional<CachedRecommendations> cached = configSync.loadConfiguration(CACHE_KEY, CachedRecommendations.class);
        log.info("Cache present == {}", cached.isPresent());
        if (cached.isPresent()) {
            long freshness = System.currentTimeMillis() - cached.get().getTimestamp();
            if (freshness < 0 || freshness > CACHE_TTL) {
                log.info("Cached values are stale. Starting from scratch.");
            } else {
                cached.get().getEntries().entrySet().stream().filter(x -> x.getValue().isValid())
                        .forEach(x -> entries.put(x.getKey(), x.getValue()));
            }
        }
        log.info("Initial entries size {}", entries.size());

        // Bootstrap recommendations
        new Thread(() -> {
            try {
                inderesConnector.queryRecommendations();
            } catch (IOException | InterruptedException e) {
                log.error("Failed to bootstrap recommendations ", e);
            }
        }).start();
    }

    private Optional<AssetPriceIntraInfo> queryCurrentPrice(String isin) {
        log.info("Querying asset price for {}", isin);
        Optional<AssetPriceIntraInfo> price = Optional.empty();
        try {
            price = yahooConnector.queryCurrentIntraPriceInfo(isin);
        } catch (IOException e) {
            log.error("Connection to yahoo finance failed", e);
        } catch (InterruptedException e) {
            log.error("Connection to yahoo finance timed out", e);
        }
        return price;
    }

    private String buildRecommendationChange(RecommendationChange recommendationChange){
        Optional<AssetPriceIntraInfo> currentPrice = queryCurrentPrice(recommendationChange.getIsin());
        return recommendationChange.buildNotificationMessage(currentPrice.orElse(null));
    }

    private void notifyRecommendationChanges(Set<RecommendationChange> changes) {
        // Some heuristic check
        if(changes.stream().filter(x -> x instanceof RemovedRecommendation).count() > 5) {
            log.error("Received unfollow for too many stocks. Skipping notify");
            return;
        }
        if(changes.stream().filter(x -> x instanceof NewRecommendation).count() > 5) {
            log.error("Received follow for too many stocks. Skipping notify");
            return;
        }
        if (!changes.isEmpty()) {
            log.info("Found recommendation changes in {} stocks", changes.size());
            Config config = configLoader.getConfig();
            List<Long> channels = config.getServers().stream()
                    .map(ServerConfig::getRecommendationChannel)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!channels.isEmpty()) {
                log.info("Sending message");
                try {
                    changes.stream().map(this::buildRecommendationChange).forEach(x -> eventCore.sendMessage(channels, x, null));
                } catch (Exception e) {
                    log.error("Error while sending recommendation updates", e);
                }
                log.info("Notified channels about recommendation changes");
            } else {
                log.info("No channels to notify");
            }
        }
    }

    private void saveRecommendations(Map<String, RecommendationEntry> recommendations) {
        CachedRecommendations cache = new CachedRecommendations(System.currentTimeMillis(), recommendations);
        if (configSync.saveConfiguration(CACHE_KEY, cache)) {
            log.info("Saved recommendations.");
        } else {
            log.error("Failed to save recommendations.");
        }
    }

    @Async
    /* Once a day */
    @Scheduled(cron = "0 0 * ? * *", zone = "Europe/Helsinki")
    /**
     * Saves latest recommendations to ensure that the timestamp value is fresh.
     */
    public void saveRecommendations() {
        log.info("Scheduled saving recommendations task.");
        synchronized (entries) {
            saveRecommendations(entries);
        }
    }

    @Async
    /* Every other minute */
    @Scheduled(cron = "2 0/2 * ? * *", zone = "Europe/Helsinki")
    public void invoke() {
        if (failureTempCounter > 0) {
            log.info("Skipping inderes recommendation query due to exponential backoff. Counter={}", failureTempCounter);
            failureTempCounter--;
            return;
        }
        try {
            Map<String, RecommendationEntry> newRecommendations = inderesConnector.queryRecommendationsMap();
            Map<String, RecommendationEntry> existingRecommendations;
            synchronized (entries) {
                existingRecommendations = new HashMap<>(entries);
            }
            Set<RecommendationChange> changedRecommendations = newRecommendations.entrySet()
                    .stream()
                    .filter(x -> Optional.ofNullable(existingRecommendations.get(x.getKey()))
                            .map(y -> y.hasChanged(x.getValue()))
                            .orElse(false)).map(x -> new ChangedRecommendation(existingRecommendations.get(x.getKey()), x.getValue()))
                    .collect(Collectors.toSet());
            if (existingRecommendations.size() > 0) {
                // Check for newly followed stocks
                newRecommendations.entrySet().stream().filter(x -> !existingRecommendations.containsKey(x.getKey()))
                        .forEach(x -> changedRecommendations.add(new NewRecommendation(x.getValue())));
                // Check for those stocks that are no longer followed
                existingRecommendations.entrySet().stream().filter(x -> !newRecommendations.containsKey(x.getKey()))
                        .forEach(x -> changedRecommendations.add(new RemovedRecommendation(x.getValue())));
            }
            // Refresh newRecommendations
            synchronized (entries) {
                entries.clear();
                entries.putAll(newRecommendations);
            }

            // Inderes can change recommendation values before changing the actual date of the recommendation
            // Let's avoid this by updating the recommendation time to current
            notifyRecommendationChanges(changedRecommendations);
            failureCounter = 0;
            // Save the changes.
            if (changedRecommendations.size() > 0 || existingRecommendations.isEmpty()) {
                saveRecommendations(entries);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve recommendations from inderes", e);
            failureCounter = Math.min(failureCounter + 1, Integer.MAX_VALUE - 1);
        }
        failureTempCounter = failureCounter;
    }
}

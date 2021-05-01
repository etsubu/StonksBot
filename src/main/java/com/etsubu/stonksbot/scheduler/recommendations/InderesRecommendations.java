package com.etsubu.stonksbot.scheduler.recommendations;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.inderes.InderesConnector;
import com.etsubu.stonksbot.scheduler.Schedulable;
import com.etsubu.stonksbot.scheduler.SchedulerService;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InderesRecommendations implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(InderesRecommendations.class);
    // ~3 days freshness
    private static final long FRESHNESS_WINDOW = 1000 * 60 * 60 * 24 * 3;
    private static final int DELAY = 300; // 5min
    // This is used for exponential backoff in case the server fails to respond
    private int failureCounter = 0;
    private int failureTempCounter = 0;
    private final Map<String, RecommendationEntry> entries;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;
    private final InderesConnector inderesConnector;

    public InderesRecommendations(SchedulerService schedulerService, EventCore eventCore, ConfigLoader configLoader,
                                  InderesConnector inderesConnector) {
        this.eventCore = eventCore;
        this.configLoader = configLoader;
        this.inderesConnector = inderesConnector;
        entries = new HashMap<>();
        schedulerService.registerTask(this, DELAY);
    }

    private String buildRecommendationChange(Set<Pair<RecommendationEntry, RecommendationEntry>> changes) {
        StringBuilder builder = new StringBuilder(64 * changes.size());
        builder.append("```\n(Inderes)\nSuositusmuutokset:\n");
        for(var v : changes) {
            builder.append("Nimi: ").append(v.getFirst().getName()).append('\n');
            builder.append("Tavoitehinta: ").append(v.getFirst().getTarget()).append(" -> ").append(v.getSecond().getTarget()).append('\n');
            builder.append("Suositus: ").append(v.getFirst().getRecommendationText()).append(" -> ").append(v.getSecond().getRecommendationText()).append('\n');
            builder.append("Riski: ").append(v.getFirst().getRisk()).append(" -> ").append(v.getSecond().getRisk()).append("\n--------------```");
        }
        return builder.toString();
    }

    private void notifyRecommendationChanges(Set<Pair<RecommendationEntry, RecommendationEntry>> changes) {
        if(!changes.isEmpty()) {
            log.info("Found recommendation changes in {} stocks", changes.size());
            Config config = configLoader.getConfig();
            List<Long> channels = config.getServers().stream()
                    .map(ServerConfig::getRecommendationChannel)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if(!channels.isEmpty()) {
                log.info("Sending message");
                eventCore.sendMessage(channels, buildRecommendationChange(changes), null);
                log.info("Notified channels about recommendation changes");
            } else {
                log.info("No channels to notify");
            }
        }
    }

    @Override
    public void invoke() {
        if(failureTempCounter > 0) {
            log.info("Skipping inderes recommendation query due to exponential backoff. Counter={}", failureTempCounter);
            failureTempCounter--;
            return;
        }
        try {
            Map<String, RecommendationEntry> recommendations = inderesConnector.queryRecommendationsMap();
            Map<String, RecommendationEntry> existingRecommendations;
            synchronized (entries) {
                existingRecommendations = new HashMap<>(entries);
            }
            Set<Pair<RecommendationEntry, RecommendationEntry>> changedRecommendations =
                    recommendations.entrySet().stream()
                    .filter(x -> Optional.ofNullable(existingRecommendations.get(x.getKey()))
                            .map(y -> y.hasChanged(x.getValue())).orElse(false))
                    .map(x-> new Pair<>(existingRecommendations.get(x.getKey()), x.getValue()))
                    .collect(Collectors.toSet());
            // These are the recommendations that have at least 3 days between last change. This is used to avoid an issue
            // Where inderes changes recommendation without updating the date of recommendation at the same time and the date
            // is actually updated during the next day
            // Alternatively if the actual recommendation values have changed then display those always
            Set<Pair<RecommendationEntry, RecommendationEntry>> freshRecommendations = changedRecommendations.stream()
                    .filter(x -> Math.abs(x.first.getLastUpdated() - x.second.getLastUpdated()) > FRESHNESS_WINDOW
                            || x.first.hasRecommendationChanged(x.second))
                    .collect(Collectors.toSet());
            // Refresh recommendations
            synchronized (entries) {
                // Delete those that are not followed by inderes anymore
                entries.entrySet().removeIf(x -> !recommendations.containsKey(x.getKey()));
                // Add new newly followed
                recommendations.values().stream().filter(x -> !entries.containsKey(x.getIsin())).forEach(x -> entries.put(x.getIsin(), x));
                // Updated those that had actually changed
                changedRecommendations.stream().map(x -> x.second).forEach(x -> entries.put(x.getIsin(), x));
            }
            // Inderes can change recommendation values before changing the actual date of the recommendation
            // Let's avoid this by updating the recommendation time to current
            notifyRecommendationChanges(freshRecommendations);
            failureCounter = 0;
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve recommendations from inderes", e);
            failureCounter = Math.min(failureCounter + 1, Integer.MAX_VALUE - 1);
        }
        failureTempCounter = failureCounter;
    }
}

package Core.Schedulers.AnalystRecommendations;

import Core.Configuration.Config;
import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import Core.Discord.EventCore;
import Core.InderesAPI.DataStructures.RecommendationEntry;
import Core.InderesAPI.InderesConnector;
import Core.Schedulers.Schedulable;
import Core.Schedulers.SchedulerService;
import Core.Utilities.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InderesRecommendations implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(InderesRecommendations.class);
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
            // Refresh recommendations
            synchronized (entries) {
                entries.clear();
                entries.putAll(recommendations);
            }
            notifyRecommendationChanges(changedRecommendations);
            failureCounter = 0;
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve recommendations from inderes", e);
            failureCounter = Math.min(failureCounter + 1, Integer.MAX_VALUE - 1);
        }
        failureTempCounter = failureCounter;
    }
}

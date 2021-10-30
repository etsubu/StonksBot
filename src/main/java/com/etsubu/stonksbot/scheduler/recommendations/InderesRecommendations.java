package com.etsubu.stonksbot.scheduler.recommendations;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.inderes.InderesConnector;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.utility.Pair;
import com.etsubu.stonksbot.yahoo.YahooConnector;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class InderesRecommendations {
    private static final Logger log = LoggerFactory.getLogger(InderesRecommendations.class);
    // ~3 days freshness
    private static final long FRESHNESS_WINDOW = 1000 * 60 * 60 * 24 * 3;
    private static final int DELAY = 300; // 5min, 300s
    // This is used for exponential backoff in case the server fails to respond
    private int failureCounter = 0;
    private int failureTempCounter = 0;
    private final Map<String, RecommendationEntry> entries;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;
    private final InderesConnector inderesConnector;
    private final YahooConnector yahooConnector;

    public InderesRecommendations(EventCore eventCore, ConfigLoader configLoader,
                                  InderesConnector inderesConnector, YahooConnector yahooConnector) {
        this.eventCore = eventCore;
        this.configLoader = configLoader;
        this.inderesConnector = inderesConnector;
        this.yahooConnector = yahooConnector;
        entries = new HashMap<>();
        // Bootstrap recommendations
        new Thread(() -> {
            try {
                inderesConnector.queryRecommendations();
            } catch (IOException | InterruptedException e) {
                log.error("Failed to bootstrap recommendations ", e);
            }
        }).start();
    }

    private String formatChangePercentage(String from, String to) {
        try {
            Num f = DecimalNum.valueOf(from);
            Num t = DecimalNum.valueOf(to);
            if (f.isZero()) {
                return "-";
            }
            Num change = (t.minus(f)).dividedBy(f);
            return DoubleTools.roundToFormat(change.multipliedBy(DecimalNum.valueOf(100)).doubleValue());
        } catch (NumberFormatException e) {
            log.error("Invalid number", e);
            return "-";
        }
    }

    private Optional<AssetPriceIntraInfo> queryCurrentPrice(RecommendationEntry entry) {
        log.info("Querying asset price for {}", entry.getIsin());
        Optional<AssetPriceIntraInfo> price = Optional.empty();
        try {
            price = yahooConnector.queryCurrentIntraPriceInfo(entry.getIsin());
        } catch (IOException e) {
            log.error("Connection to yahoo finance failed", e);
        } catch (InterruptedException e) {
            log.error("Connection to yahoo finance timed out", e);
        }
        return price;
    }

    private String buildRecommendationChange(Set<Pair<RecommendationEntry, RecommendationEntry>> changes) {
        StringBuilder builder = new StringBuilder(64 * changes.size());
        for (var v : changes) {
            Optional<AssetPriceIntraInfo> currentPrice = queryCurrentPrice(v.second);
            var from = v.getFirst();
            var to = v.getSecond();
            Num targetPrice = DecimalNum.valueOf(v.second.getTarget().replaceAll(",", "."));
            if(from != null) {
                builder.append("```\n(Inderes)\nSuositusmuutos:");
                builder.append("\nNimi: ").append(from.getName()).append('\n');
                builder.append("Tavoitehinta: ").append(from.getTarget()).append(" -> ").append(to.getTarget()).append(" (")
                        .append(formatChangePercentage(from.getTarget(), to.getTarget())).append("%)")
                        .append('\n');
                currentPrice.ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(to.getCurrency())
                        .append("\nNousuvara: ")
                        .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                        .append("%\n"));
                builder.append("Suositus: ").append(from.getRecommendationText()).append(" -> ").append(to.getRecommendationText()).append('\n');
                builder.append("Riski: ").append(from.getRisk()).append(" -> ").append(to.getRisk()).append("\n--------------```");
            } else {
                builder.append("```\n(Inderes)\nSeurannan aloitus:");
                builder.append("\nNimi: ").append(to.getName()).append('\n');
                builder.append("Tavoitehinta: ").append(to.getTarget()).append('\n');
                currentPrice.ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(to.getCurrency())
                        .append("\nNousuvara: ")
                        .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                        .append("%\n"));
                builder.append("Suositus: ").append(to.getRecommendationText()).append('\n');
                builder.append("Riski: ").append(to.getRisk()).append("\n--------------```");
            }
        }
        return builder.toString();
    }

    private void notifyRecommendationChanges(Set<Pair<RecommendationEntry, RecommendationEntry>> changes) {
        if (!changes.isEmpty()) {
            log.info("Found recommendation changes in {} stocks", changes.size());
            Config config = configLoader.getConfig();
            List<Long> channels = config.getServers().stream()
                    .map(ServerConfig::getRecommendationChannel)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!channels.isEmpty()) {
                log.info("Sending message");
                eventCore.sendMessage(channels, buildRecommendationChange(changes), null);
                log.info("Notified channels about recommendation changes");
            } else {
                log.info("No channels to notify");
            }
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
            Map<String, RecommendationEntry> recommendations = inderesConnector.queryRecommendationsMap();
            Map<String, RecommendationEntry> existingRecommendations;
            synchronized (entries) {
                existingRecommendations = new HashMap<>(entries);
            }
            Set<Pair<RecommendationEntry, RecommendationEntry>> changedRecommendations =
                    recommendations.entrySet().stream()
                            .filter(x -> Optional.ofNullable(existingRecommendations.get(x.getKey()))
                                    .map(y -> y.hasChanged(x.getValue())).orElse(false))
                            .map(x -> new Pair<>(existingRecommendations.get(x.getKey()), x.getValue()))
                            .collect(Collectors.toSet());
            // Check for newly followed stocks
            if(existingRecommendations.size() > 0) {
                recommendations.entrySet().stream().filter(x -> !existingRecommendations.containsKey(x.getKey()))
                        .forEach(x -> changedRecommendations.add(new Pair<>(null, x.getValue())));
            }
            // These are the recommendations that have at least 3 days between last change. This is used to avoid an issue
            // Where inderes changes recommendation without updating the date of recommendation at the same time and the date
            // is actually updated during the next day
            // Alternatively if the actual recommendation values have changed then display those always
            Set<Pair<RecommendationEntry, RecommendationEntry>> freshRecommendations = changedRecommendations.stream()
                    .filter(x -> x.first == null || (Math.abs(x.first.getLastUpdated() - x.second.getLastUpdated()) > FRESHNESS_WINDOW
                            || x.first.hasRecommendationChanged(x.second)))
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

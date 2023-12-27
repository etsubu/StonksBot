package com.etsubu.stonksbot.inderes;

import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Connector that integrates to Inderes API for fetching different OMXH stock recommendations
 *
 * @author etsubu
 */
@Component
public class InderesConnectorImpl implements InderesConnector {
    private static final Logger log = LoggerFactory.getLogger(InderesConnectorImpl.class);
    private static final Gson gson = new Gson();
    private static final String INDERES_RECOMMENDATION_URL = "https://classic.inderes.fi/fi/rest/inderes_numbers_recommendations.json";
    private final Map<String, RecommendationEntry> entries;

    public InderesConnectorImpl() {
        entries = new HashMap<>();
    }

    public Set<RecommendationEntry> queryRecommendations() throws IOException, InterruptedException {
        if (entries.isEmpty()) {
            // Lazy load if the scheduled task has not ran yet
            queryRecommendationsMap();
        }
        synchronized (entries) {
            return new HashSet<>(entries.values());
        }
    }


    /**
     * Requests all stock recommendations from Inderes
     *
     * @return List of stock recommendations
     * @throws IOException          Thrown for connection errors
     * @throws InterruptedException Thrown for connection timeouts
     */
    public Map<String, RecommendationEntry> queryRecommendationsMap() throws IOException, InterruptedException {
        Map<String, RecommendationEntry> recommendations = new HashMap<>(64);
        Optional<String> response = HttpApi.sendGet(INDERES_RECOMMENDATION_URL);
        String body = response.orElseThrow(() -> new IOException("Failed to receive recommendation data from inderes"));
        JSONObject baseObject = new JSONObject(body);
        baseObject.keySet().forEach(x -> {
            try {
                RecommendationEntry entry = gson.fromJson(baseObject.getJSONObject(x).toString(), RecommendationEntry.class);
                if (entry.getIsin() != null && entry.isValid()) {
                    recommendations.put(entry.getIsin(), entry);
                }
            } catch (JsonParseException e) {
                log.error("Failed to parse recommendation json entry ", e);
            }
        });
        // Update last updated timestamp
        synchronized (entries) {
            entries.clear();
            entries.putAll(recommendations);
        }
        if(recommendations.size() <= 1) {
            log.error("Received too few recommendations. Raising exception");
            throw new IOException("Too few recommendations " + recommendations.size());
        }
        return recommendations;
    }
}

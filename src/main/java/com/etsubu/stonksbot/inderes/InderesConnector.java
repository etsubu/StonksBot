package com.etsubu.stonksbot.inderes;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface InderesConnector {
    Set<RecommendationEntry> queryRecommendations() throws IOException, InterruptedException;

    Map<String, RecommendationEntry> queryRecommendationsMap() throws IOException, InterruptedException;
}

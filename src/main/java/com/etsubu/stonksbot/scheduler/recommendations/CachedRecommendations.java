package com.etsubu.stonksbot.scheduler.recommendations;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class CachedRecommendations {
    private final long timestamp;
    private final Map<String, RecommendationEntry> entries;
}

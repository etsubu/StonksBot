package com.etsubu.stonksbot.inderes.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * Single stock recommendation returned from Inderes API
 *
 * @author etsubu
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class RecommendationEntry {
    /* This is internal value and not from inderes */
    private Long lastUpdated;
    private String isin;
    private String name;
    @SerializedName("date_of_recommendation")
    private String date;
    @SerializedName("target_price")
    private String target;
    private String currency;
    private String recommendation;
    @SerializedName("risk_level")
    private String risk;

    /**
     * Converts numeric recommendation to descriptive text format
     *
     * @return Stock recommendation in finnish
     */
    public String getRecommendationText() {
        if (recommendation == null || recommendation.isEmpty()) {
            return "tuntematon suositus";
        }
        return switch (recommendation.charAt(0)) {
            case '1' -> "myy";
            case '2' -> "vähennä";
            case '3' -> "pidä";
            case '4' -> "lisää";
            case '5' -> "osta";
            default -> "tuntematon suositus";
        };
    }

    public long getLastUpdated() {
        if (lastUpdated == null) {
            lastUpdated = System.currentTimeMillis();
        }
        return lastUpdated;
    }

    public void updateLastUpdated() {
        lastUpdated = System.currentTimeMillis();
    }

    public boolean hasChanged(RecommendationEntry entry) {
        return Objects.equals(isin, entry.isin) && (!Objects.equals(date, entry.getDate())
                || !Objects.equals(target, entry.getTarget())
                || !Objects.equals(recommendation, entry.getRecommendation())
                || !Objects.equals(risk, entry.getRisk()));
    }

    public boolean hasRecommendationChanged(RecommendationEntry entry) {
        return Objects.equals(isin, entry.isin) && (!Objects.equals(target, entry.getTarget())
                || !Objects.equals(recommendation, entry.getRecommendation())
                || !Objects.equals(risk, entry.getRisk()));
    }

    @Override
    public int hashCode() {
        return isin != null ? isin.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        return Objects.equals(((RecommendationEntry) o).getIsin(), getIsin());
    }
}

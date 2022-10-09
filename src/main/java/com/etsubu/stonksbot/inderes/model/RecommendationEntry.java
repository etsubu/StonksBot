package com.etsubu.stonksbot.inderes.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Single stock recommendation returned from Inderes API
 *
 * @author etsubu
 */
@Getter
@AllArgsConstructor
@ToString
public class RecommendationEntry {
    private final String isin;
    private final String name;
    @SerializedName("date_of_recommendation")
    private final String date;
    @SerializedName("target_price")
    private final String target;
    private final String currency;
    private final String recommendation;
    @SerializedName("risk_level")
    private final String risk;

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

    public boolean isValid() {
        return name != null && target != null && currency != null && recommendation != null && risk != null && date != null && isin != null;
    }

    public boolean hasChanged(RecommendationEntry entry) {
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

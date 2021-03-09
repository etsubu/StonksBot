package Core.InderesAPI.DataStructures;

import Core.Utilities.TimeUtils;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Single stock recommendation returned from Inderes API
 * @author etsubu
 */
@Getter
@Setter
@AllArgsConstructor
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
     * @return Stock recommendation in finnish
     */
    public String getRecommendationText() {
        if (recommendation == null || recommendation.isEmpty()) {
            return "tuntematon suositus";
        }
        switch (recommendation.charAt(0)) {
            case '1': return "myy";
            case '2': return "vähennä";
            case '4': return "lisää";
            case '5': return "osta";
            default: return "tuntematon suositus";
        }
    }

    public long getLastUpdated() {
        if(lastUpdated == null) {
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

    public boolean hasDaysBetweenChange(RecommendationEntry entry, int days) {
        ZonedDateTime time = TimeUtils.parseTime(entry.getDate());
        ZonedDateTime ownTime = TimeUtils.parseTime(getDate());
        if(time.equals(ownTime)) {
            return false;
        }
        if(time.isAfter(ownTime)) {
            return time.isAfter(ownTime.plusDays(days));
        }
        return ownTime.isAfter(time.plusDays(days));
    }

    @Override
    public int hashCode() {
        return isin != null ? isin.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()) {
            return false;
        }
        return Objects.equals(((RecommendationEntry)o).getIsin(), getIsin());
    }
}

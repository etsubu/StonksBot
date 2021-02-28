package Core.InderesAPI.DataStructures;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Single stock recommendation returned from Inderes API
 * @author etsubu
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
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

    public boolean hasChanged(RecommendationEntry entry) {
        return Objects.equals(isin, entry.isin) && !Objects.equals(date, entry.getDate());
    }
}

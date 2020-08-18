package Core.Lunch;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LunchList {
    @SerializedName("RestaurantName")
    private final String restaurantName;
    @SerializedName("RestaurantUrl")
    private final String restaurantUrl;
    @SerializedName("Footer")
    private final String footer;
    @SerializedName("MenusForDays")
    private final List<LunchDay> lunchDays;

    public LunchList(String restaurantName, String restaurantUrl, String footer, List<LunchDay> lunchDays) {
        this.restaurantName = restaurantName;
        this.restaurantUrl = restaurantUrl;
        this.footer = footer;
        this.lunchDays = lunchDays;
    }

    public String getRestaurantName() { return restaurantName; }

    public String getRestaurantUrl() { return restaurantUrl; }

    public String getFooter() { return footer; }

    public List<LunchDay> getLunchDays() { return Collections.unmodifiableList(lunchDays); }

    public List<LunchDay> getLunchDays(int day) {
        return lunchDays.stream()
                .filter(x -> x.getDate().getDayOfMonth() == day)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(restaurantName).append('\n');
        lunchDays.forEach(x -> builder.append(x).append('\n'));
        return builder.toString();
    }
}

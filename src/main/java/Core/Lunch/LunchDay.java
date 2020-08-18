package Core.Lunch;

import com.google.gson.annotations.SerializedName;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class LunchDay {
    @SerializedName("Date")
    private final String date;
    @SerializedName("LunchTime")
    private final String lunchTime;
    @SerializedName("SetMenus")
    private final List<LunchOption> lunchOptions;

    public LunchDay(String date, String lunchTime, List<LunchOption> lunchOptions) {
        this.date = date;
        this.lunchTime = lunchTime;
        this.lunchOptions = lunchOptions;
    }

    public ZonedDateTime getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return ZonedDateTime.parse(date, formatter);
    }

    public String getLunchTime() { return lunchTime; }

    public List<LunchOption> getLunchOptions() { return Collections.unmodifiableList(lunchOptions); }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(date)
                .append('\n');
        lunchOptions.forEach(x -> builder.append(x).append('\n'));
        return builder.toString();
    }
}

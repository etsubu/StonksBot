package Core.Lunch;

import Core.HTTP.HttpApi;
import Core.Utilities.Pair;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LunchQuery {
    private final ZoneId zoneId = ZoneId.of("Europe/Helsinki");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger log = LoggerFactory.getLogger(LunchQuery.class);
    private final HttpClient client;
    private final Gson gson;
    private final List<Pair<String, String>> restaurants;

    public LunchQuery() {
        this.restaurants = new ArrayList<>();
        restaurants.add(new Pair<>("https://www.semma.fi/api/restaurant/menu/day?date=%s&language=fi&restaurantPageId=207735", "Ravintola Piato"));
        restaurants.add(new Pair<>("https://www.semma.fi/api/restaurant/menu/day?date=%s&language=fi&restaurantPageId=207659", "Ravintola Maija"));
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL).build();
        this.gson = new Gson();
    }

    public synchronized List<LunchMenu> queryLunchList() throws IOException, InterruptedException {
        ZonedDateTime time = ZonedDateTime.now(zoneId);
        if(time.getHour() >= 18) {
            // Look for tomorrow's lunch
            time = time.plusDays(1);
        }
        if(time.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            time = time.plusDays(2);
        } else if(time.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            time = time.plusDays(3);
        }
        String asciiDate = formatter.format(time);
        log.info("Query lunch list");
        List<LunchMenu> menus = new ArrayList<>();
        for(Pair<String, String> restaurant : restaurants) {
            String url = String.format(restaurant.first, asciiDate);
            HttpApi.sendGet(url).ifPresent(x -> {
                LunchMenu menu = gson.fromJson(x, LunchResponse.class).getMenu();
                menu.setRestaurantName(restaurant.second);
                menus.add(menu);
            });
        }
        return Collections.unmodifiableList(menus);
    }
}

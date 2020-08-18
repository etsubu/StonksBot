package Core.Lunch;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LunchQuery {
    private static final Logger log = LoggerFactory.getLogger(LunchQuery.class);
    private final HttpClient client;
    private final List<LunchList> lunchList;
    private final Gson gson;
    private final String[] restaurantUrls;

    public LunchQuery() {
        this.restaurantUrls = new String[]{
                "https://www.semma.fi/modules/json/json/Index?costNumber=1408&language=fi",
                "https://www.semma.fi/modules/json/json/Index?costNumber=1402&language=fi"};
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL).build();
        this.gson = new Gson();
        this.lunchList = new ArrayList<>(restaurantUrls.length);
    }

    public synchronized List<LunchList> queryLunchList() throws IOException, InterruptedException {
        log.info("Query lunch list");
        lunchList.clear();
        for(String url : restaurantUrls) {
            log.info("Requesting " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                lunchList.clear();
                log.info("Core.HTTP request returned status code: " + response.statusCode());
                throw new IOException("Core.HTTP request returned: " + response.statusCode());
            }
            lunchList.add(gson.fromJson(response.body(), LunchList.class));
        }
        return Collections.unmodifiableList(lunchList);
    }

    public synchronized List<LunchList> getLunchList(int day) throws IOException, InterruptedException {
        boolean contains = false;
        for(LunchList lunch : lunchList) {
            if(lunch.getLunchDays().stream().anyMatch(x -> x.getDate().getDayOfMonth() == day)) {
                contains = true;
                break;
            }
        }
        return contains ? lunchList : queryLunchList();
    }
}

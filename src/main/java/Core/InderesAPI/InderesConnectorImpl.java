package Core.InderesAPI;

import Core.HTTP.HttpApi;
import Core.InderesAPI.DataStructures.RecommendationEntry;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class InderesConnectorImpl implements InderesConnector {
    private static final Logger log = LoggerFactory.getLogger(InderesConnectorImpl.class);
    private static final Gson gson = new Gson();
    private static final String INDERES_RECOMMENDATION_URL = "https://www.inderes.fi/fi/rest/inderes_numbers_recommendations.json";
    private static final long CACHE_TTL = 1000 * 60 * 60; // 1 hour cache for recommendations
    private final List<RecommendationEntry> entries;
    private long lastQueried;

    public InderesConnectorImpl() {
        lastQueried = 0;
        entries = new ArrayList<>();
    }

    public synchronized List<RecommendationEntry> queryRecommendations() throws IOException, InterruptedException {
        if(entries.isEmpty() || (System.currentTimeMillis() - lastQueried) > CACHE_TTL) {
            log.info("Requesting recommendation data from inderes");
            lastQueried = System.currentTimeMillis();
            Optional<String> response = HttpApi.sendGet(INDERES_RECOMMENDATION_URL);
            String body = response.orElseThrow(() -> new IOException("Failed to receive recommendation data from inderes"));
            JSONObject baseObject = new JSONObject(body);
            entries.clear();
            baseObject.keySet().forEach(x -> entries.add(gson.fromJson(baseObject.getJSONObject(x).toString(), RecommendationEntry.class)));
            log.info("Loaded recommendations for {} omxh/first north stocks", entries.size());
            return Collections.unmodifiableList(entries);
        } else {
            log.info("Recommendation data cache is still fresh, returning cache data");
            return Collections.unmodifiableList(entries);
        }
    }
}

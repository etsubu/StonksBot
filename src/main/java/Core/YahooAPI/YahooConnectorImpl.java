package Core.YahooAPI;

import Core.YahooAPI.DataStructures.AssetPriceIntraInfo;
import Core.YahooAPI.DataStructures.DataResponse;
import Core.YahooAPI.DataStructures.GeneralResponse;
import Core.YahooAPI.DataStructures.PriceChart.PriceMeta.ChartResult;
import Core.YahooAPI.DataStructures.SearchStructures.SearchResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
public class YahooConnectorImpl implements YahooConnector{
    private static final Logger log = LoggerFactory.getLogger(YahooConnectorImpl.class);
    private static final String FUNDAMENT_BASE_URL = "https://query%d.finance.yahoo.com/v10/finance/quoteSummary/%s?modules=%s";
    private static final String PRICE_BASE_URL = "https://query%d.finance.yahoo.com/v8/finance/chart/%s?symbol=%s&period1=%d&period2=%d&interval=%s";
    private static final String SEARCH_BASE_URL = "https://query%d.finance.yahoo.com/v1/finance/search?q=%s&quotesCount=1&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query";
    private static final String DEFAULT_STATISTICS = "defaultKeyStatistics";
    private final HttpClient client;
    private int loadBalanceIndex;
    private final TickerStorage tickerStorage;
    private final Gson gson;

    public YahooConnectorImpl() {
        client = HttpClient.newHttpClient();
        loadBalanceIndex = 1;
        tickerStorage = new TickerStorage();
        gson = new Gson();
    }

    private int getLoadBalanceIndex() {
        int index = loadBalanceIndex;
        loadBalanceIndex = (loadBalanceIndex == 1) ? 2 : 1;
        return index;
    }

    private Optional<String> requestHttp(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        log.info("Sending request to {}", uri.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() != 200) {
            log.error("Request returned invalid status code {}", response.statusCode());
            return Optional.empty();
        }
        return Optional.of(response.body());
    }

    public Optional<String> findTicker(String keyword) throws IOException, InterruptedException {
        Optional<String> ticker = tickerStorage.findTicker(keyword);
        if(ticker.isPresent()) {
            log.info("Found ticker from cache {}=>{}", keyword, ticker.get());
            return ticker;
        }
        // Query from yahoo finance
        String requestUrl = String.format(SEARCH_BASE_URL, getLoadBalanceIndex(), keyword);
        Optional<String> response = requestHttp(requestUrl);
        if(response.isPresent()) {
            SearchResponse searchResults = gson.fromJson(response.get(), SearchResponse.class);
            if(searchResults.getQuotes().isEmpty()) {
                return Optional.empty();
            }
            String queriedTicker = searchResults.getQuotes().get(0).getSymbol();
            tickerStorage.setShortcut(keyword, queriedTicker);
            return Optional.of(queriedTicker);
        }
        return Optional.empty();
    }

    public Optional<BarSeries> queryIntraPriceChart(String keyword) throws IOException, InterruptedException {
        String ticker = findTicker(keyword)
                .orElseThrow(() -> new IOException("Could not find any assets with keyword " + keyword));
        long unixTime = System.currentTimeMillis() / 1000;
        unixTime -= (60 * 60 * 24); // One day backwards
        String requestUrl = String.format(PRICE_BASE_URL, getLoadBalanceIndex(), ticker, ticker, unixTime, 9999999999L, "1m");
        Optional<String> body = requestHttp(requestUrl);
        if(body.isPresent()) {
            return Optional.of(ChartResult.buildChartResultFromJson(body.get()).getIntraBarSeries());
        }
        return Optional.empty();
    }

    public Optional<AssetPriceIntraInfo> queryCurrentIntraPriceInfo(String keyword) throws IOException, InterruptedException {
        return queryIntraPriceChart(keyword).map(AssetPriceIntraInfo::new);
    }

    public Optional<DataResponse> queryData(String ticker, String... typeList) throws IOException, InterruptedException {
        String modules = String.join("%2C", typeList);
        String requestUrl = String.format(FUNDAMENT_BASE_URL, getLoadBalanceIndex(), ticker, modules);
        return requestHttp(requestUrl).map(GeneralResponse::parseResponse);
    }
}

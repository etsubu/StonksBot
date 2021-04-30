package com.etsubu.stonksbot.yahoo;

import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import com.etsubu.stonksbot.yahoo.model.DataResponse;
import com.etsubu.stonksbot.yahoo.model.DataValue;
import com.etsubu.stonksbot.yahoo.model.fundament.FundaValue;
import com.etsubu.stonksbot.yahoo.model.fundament.FundamentEntry;
import com.etsubu.stonksbot.yahoo.model.GeneralResponse;
import com.etsubu.stonksbot.yahoo.model.price.ChartResult;
import com.etsubu.stonksbot.utility.Pair;
import com.etsubu.stonksbot.yahoo.model.search.SearchResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class YahooConnectorImpl implements YahooConnector{
    private static final Logger log = LoggerFactory.getLogger(YahooConnectorImpl.class);
    private static final String FUNDAMENT_BASE_URL = "https://query%d.finance.yahoo.com/v10/finance/quoteSummary/%s?modules=%s";
    private static final String PRICE_BASE_URL = "https://query%d.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=2d";
    private static final String SEARCH_BASE_URL = "https://query%d.finance.yahoo.com/v1/finance/search?q=%s&quotesCount=1&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query";
    private static final String FUNDAMENTAL_TIMESERIES_URL = "https://query%d.finance.yahoo.com/ws/fundamentals-timeseries/v1/finance/timeseries/%s?&type=%s&period1=0&period2=%d";
    public static final String DEFAULT_STATISTICS = "defaultKeyStatistics";
    public static final String CALENDAR_EVENTS = "calendarEvents";
    public static final String ASSET_PROFILE = "assetProfile";
    public static final String TRAILING_FREE_CASH_FLOW = "trailingFreeCashFlow";
    public static final String TRAILING_MARKET_CAP = "trailingMarketCap";
    private final HttpClient client;
    private int loadBalanceIndex;
    private final TickerStorage tickerStorage;
    private final Gson gson;

    public YahooConnectorImpl(TickerStorage tickerStorage) {
        client = HttpClient.newHttpClient();
        loadBalanceIndex = 1;
        gson = new Gson();
        this.tickerStorage = tickerStorage;
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

    public static Optional<Num> sumLastFourQuarters(Optional<FundamentEntry> entry) {
        if(entry.isEmpty()) {
            log.info("Argument was empty, returning empty");
            return Optional.empty();
        }
        List<FundaValue> values = entry.get().getValue();
        if(values == null || values.size() < 4) {
            log.info("Value does not have enough quarters available to calculate TTM");
            return Optional.empty();
        }
        values.sort(Comparator.comparing(FundaValue::getAsOfDate).reversed());
        Num sum = DecimalNum.valueOf(0);
        for(int i = 0; i < 4; i++) {
            Optional<DataValue> reportedValue = values.get(i).getReportedValue();
            if(reportedValue.isEmpty()){
                log.error("Missing reported value");
                return Optional.empty();
            }
            sum = sum.plus(DecimalNum.valueOf(reportedValue.get().getRaw()));
        }
        return Optional.of(sum);
    }

    public static Optional<Num> getLatestValue(Optional<FundamentEntry> entry) {
        if(entry.isEmpty()) {
            log.info("Argument was empty, returning empty");
            return Optional.empty();
        }
        List<FundaValue> values = entry.get().getValue();
        if(values == null || values.isEmpty()) {
            log.info("Value does not have enough quarters");
            return Optional.empty();
        }
        values.sort(Comparator.comparing(FundaValue::getAsOfDate).reversed());
        if(values.get(0).getReportedValue().isPresent() && values.get(0).getReportedValue().get().getRaw() != null) {
            return Optional.of(DecimalNum.valueOf(values.get(0).getReportedValue().get().getRaw()));
        }
        return Optional.empty();
    }

    public Optional<StockName> findTicker(String keyword) throws IOException, InterruptedException {
        Optional<StockName> ticker = tickerStorage.findTicker(keyword);
        if(ticker.isPresent()) {
            log.info("Found ticker from cache {}=>{}", keyword, ticker.get());
            return ticker;
        }
        // Query from yahoo finance
        String requestUrl = String.format(SEARCH_BASE_URL, getLoadBalanceIndex(), URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        Optional<String> response = requestHttp(requestUrl);
        if(response.isPresent()) {
            SearchResponse searchResults = gson.fromJson(response.get(), SearchResponse.class);
            if(searchResults.getQuotes().isEmpty()) {
                return Optional.empty();
            }
            String queriedTicker = searchResults.getQuotes().get(0).getSymbol();
            String queriedName = Optional.ofNullable(searchResults.getQuotes().get(0).getLongname()).orElse(queriedTicker);
            StockName stockName = new StockName(queriedTicker, queriedName);
            tickerStorage.setShortcut(keyword, stockName);
            return Optional.of(stockName);
        }
        return Optional.empty();
    }

    public Optional<BarSeries> queryIntraPriceChart(String ticker) throws IOException, InterruptedException {
        String requestUrl = String.format(PRICE_BASE_URL, getLoadBalanceIndex(), URLEncoder.encode(ticker, StandardCharsets.UTF_8));
        Optional<String> body = requestHttp(requestUrl);
        if(body.isPresent()) {
            return Optional.ofNullable(ChartResult.buildChartResultFromJson(body.get()).getBarSeries());
        }
        return Optional.empty();
    }

    public Optional<AssetPriceIntraInfo> queryCurrentIntraPriceInfo(String keyword) throws IOException, InterruptedException {
        StockName name = findTicker(keyword)
                .orElseThrow(() -> new IOException("Could not find any assets with keyword " + keyword));
        return queryIntraPriceChart(name.getTicker()).map(x -> new AssetPriceIntraInfo(x, name));
    }

    public Optional<DataResponse> queryData(String keyword, String... typeList) throws IOException, InterruptedException {
        StockName ticker = findTicker(keyword)
                .orElseThrow(() -> new IOException("Could not find any assets with keyword " + keyword));
        String modules = String.join("%2C", typeList);
        String requestUrl = String.format(FUNDAMENT_BASE_URL, getLoadBalanceIndex(), URLEncoder.encode(ticker.getTicker(), StandardCharsets.UTF_8), modules);
        return requestHttp(requestUrl).map(x -> GeneralResponse.parseResponse(x, ticker));
    }

    private Map<String, FundamentEntry> parseFundamentalTimeSeries(String json) throws IllegalArgumentException{
        JSONObject root = new JSONObject(json);
        root = root.getJSONObject("timeseries");
        if(root.get("error") != null && !root.get("error").toString().equalsIgnoreCase("null")) {
            log.error("Received error from yahoo finance rest api '{}', {}", root.get("error").toString(), json);
            throw new IllegalArgumentException("Yahoo finance returned error " + root.get("error").toString());
        }
        JSONArray result = root.getJSONArray("result");
        Map<String, FundamentEntry> fundamentEntryMap = new HashMap<>();
        for(int i = 0; i < result.length(); i++) {
            JSONObject jsonEntry = result.getJSONObject(i);
            FundamentEntry entry = gson.fromJson(jsonEntry.toString(), FundamentEntry.class);
            Optional<String> type = entry.getMeta().getType();
            if(type.isEmpty()) {
                log.info("Type was not available");
                continue;
            }
            log.info("Parsed type {}", type.get());
            try {
                JSONArray timeseries = jsonEntry.getJSONArray(type.get());
                Type valueEntryList = new TypeToken<ArrayList<FundaValue>>() {}.getType();
                // Set the value entry list manually
                entry.setValue(gson.fromJson(timeseries.toString(), valueEntryList));
                entry.getValue().sort(Comparator.comparing(FundaValue::getAsOfDate).reversed());
                fundamentEntryMap.put(type.get(), entry);
            } catch (JSONException e) {
                log.info("No entries for type {}", type);
            }
        }
        return fundamentEntryMap;
    }

    public Map<String, FundamentEntry> queryFundamentTimeSeries(String keyword, String... fundaments) throws IOException, InterruptedException {
        StockName name = findTicker(keyword)
                .orElseThrow(() -> new IOException("Could not find any assets with keyword " + keyword));
        String types = String.join("%2C", fundaments);
        String url = String.format(FUNDAMENTAL_TIMESERIES_URL, getLoadBalanceIndex(), name.getTicker(), types, Instant.now().getEpochSecond());
        Optional<String> response = requestHttp(url);
        if(response.isEmpty()) {
            log.error("No fundamental data for {}", name.getTicker());
            return new HashMap<>();
        }
        return parseFundamentalTimeSeries(response.get());
    }

    public Pair<StockName, Map<String, FundamentEntry>> queryFundamentTimeSeries(String keyword, List<String> fundaments) throws IOException, InterruptedException {
        StockName name = findTicker(keyword)
                .orElseThrow(() -> new IOException("Could not find any assets with keyword " + keyword));
        String types = String.join("%2C", fundaments);
        String url = String.format(FUNDAMENTAL_TIMESERIES_URL, getLoadBalanceIndex(), name.getTicker(), types, Instant.now().getEpochSecond());
        Optional<String> response = requestHttp(url);
        if(response.isEmpty()) {
            log.error("No fundamental data for {}", name.getTicker());
            return new Pair<>(name, new HashMap<>());
        }
        return new Pair<>(name, parseFundamentalTimeSeries(response.get()));
    }
}

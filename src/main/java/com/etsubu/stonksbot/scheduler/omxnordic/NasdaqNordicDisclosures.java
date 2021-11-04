package com.etsubu.stonksbot.scheduler.omxnordic;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ConfigurationSync;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.TransactionItem;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.DisclosureItem;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableAsync
public class NasdaqNordicDisclosures {
    private static final Logger log = LoggerFactory.getLogger(NasdaqNordicDisclosures.class);
    private static final String CACHE_KEY = "disclosures";
    private static final int OMXH_ID = 0;
    private static final int FIRST_NORTH_ID = 1;
    private static final String[] DISCLOSURE_URL_TEMPLATES = {
            "https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&market=Main%20Market%2C+Helsinki&cnscategory=&company=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicMainMarkets&displayLanguage=fi&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&dir=DESC&start=",
            "https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&company=&market=First%20North+Finland&cnscategory=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicFirstNorth&displayLanguage=en&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&dir=DESC&start="
    };
    private final List<Integer> LATEST_DISCLOSURE_IDS;
    private static final int DELAY_IN_TASK = 120; // 2min'
    private static final long CACHE_TTL = 1000 * 60 * 60; // 1 hour
    private final Gson gson;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;
    private final ConfigurationSync configSync;

    public NasdaqNordicDisclosures(ConfigLoader configLoader, EventCore eventCore, ConfigurationSync configSync) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        this.configSync = configSync;
        gson = new Gson();
        Optional<DisclosureCache> cache = configSync.loadConfiguration(CACHE_KEY, DisclosureCache.class);
        log.info("Cache present == {}", cache.isPresent());
        if(cache.isPresent()) {
            long freshness = System.currentTimeMillis() - cache.get().getTimestamp();
            if(freshness < 0 || freshness > CACHE_TTL) {
                log.info("Cache is stale, {} ms. Starting from scratch", freshness);
                LATEST_DISCLOSURE_IDS = Arrays.asList(-1, -1);
            } else {
                LATEST_DISCLOSURE_IDS = cache.get().getLatestIds();
                log.info("Cache loaded.");
            }
        } else {
            LATEST_DISCLOSURE_IDS = Arrays.asList(-1, -1);
        }
    }

    public Optional<List<DisclosureItem>> parseResponse(String raw) {
        int begin = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (begin == -1 || end == -1) {
            log.info("Received invalid data from omx nasdaq nordic {}", raw);
            return Optional.empty();
        }
        try {
            raw = raw.substring(begin, end + 1);
            JSONObject root = new JSONObject(raw);
            if (root.has("results")) {
                JSONObject results = root.getJSONObject("results");
                if (results.has("item")) {
                    JSONArray items = results.getJSONArray("item");
                    List<DisclosureItem> news = new ArrayList<>(items.length());
                    for (int i = 0; i < items.length(); i++) {
                        news.add(gson.fromJson(items.get(i).toString(), DisclosureItem.class));
                    }
                    if (news.isEmpty()) {
                        return Optional.empty();
                    }
                    // Sort the items
                    Collections.sort(news);
                    return Optional.of(news);
                } else {
                    log.error("Omx nordic response json did not contain results section");
                }
            } else {
                log.error("Omx nordic response json did not contain results section");
            }
        } catch (JSONException e) {
            log.error("Failed to parse json response from omx nordic {}", raw, e);
        } catch (JsonParseException e) {
            log.error("Failed to parse json item from omx nordic {}", raw, e);
        }
        return Optional.empty();
    }

    private void sendNewDisclosures(List<DisclosureItem> items) {
        log.info("Resolving disclosure attachments");
        items.forEach(DisclosureItem::resolveAttachments);
        List<Long> channelIds = configLoader.getConfig().getServers().stream().map(ServerConfig::getNewsChannel).filter(Objects::nonNull).collect(Collectors.toList());
        if (!channelIds.isEmpty()) {
            items.forEach(x -> eventCore.sendMessage(channelIds, x.toString(), x.getFiles()));
        }
    }

    private List<DisclosureItem> loadDisclosureItems(int id) throws IOException, InterruptedException {
        int start = 0;
        boolean lastIdFound = false;
        // Loop 20 items at a time until we find the latest disclosure we've seen
        Set<DisclosureItem> newDisclosures = new HashSet<>();
        while (!lastIdFound) {
            if(start > 0) {
                log.info("Querying extra items, starting with '{}'", start);
            }
            String url = DISCLOSURE_URL_TEMPLATES[id] + start;
            Optional<String> response = HttpApi.sendGet(url);
            if(response.isEmpty()) {
                return new LinkedList<>();
            }
            Optional<List<DisclosureItem>> items = parseResponse(response.get());
            if(items.isEmpty()) {
                return new LinkedList<>();
            }
            List<DisclosureItem> itemList = items.get();
            if(LATEST_DISCLOSURE_IDS.get(id) == -1 || itemList.get(itemList.size() - 1).getDisclosureId() < LATEST_DISCLOSURE_IDS.get(id)) {
                // Found earlier disclosure than the previously latest one
                lastIdFound = true;
            }
            // Collect new disclosures
            newDisclosures.addAll(items.get().stream().filter(x ->
                            Optional.ofNullable(x.getDisclosureId()).map(y -> y > LATEST_DISCLOSURE_IDS.get(id)).orElse(false) &&
                                    Optional.ofNullable(x.getLanguage()).map(y -> y.equals("fi")).orElse(false))
                    .collect(Collectors.toList()));
            start += itemList.size();
        }
        List<DisclosureItem> items = new ArrayList<>(newDisclosures);
        Collections.sort(items);
        return items;
    }

    private void processDisclosures(List<DisclosureItem> items) {
        for(int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if(item.isValid() && item.isTransaction()) {
                try {
                    TransactionItem transaction = new TransactionItem(item);
                    transaction.loadValues();
                    items.set(i, transaction);
                } catch (Exception e) {
                    log.error("Failed to map transaction", e);
                }
            }
        }
    }

    @Async
    /* Every other minute during weekdays */
    @Scheduled(cron = "5 * 7-23 ? * MON-FRI", zone = "Europe/Helsinki")
    public void invoke() {
        if (Optional.ofNullable(configLoader.getConfig().getOmxhNews()).map(x -> !Boolean.parseBoolean(x.getEnabled())).orElse(false)) {
            // Not enabled, skip
            return;
        }
        try {
            List<DisclosureItem> disclosureItems = new ArrayList<>();
            boolean initRun = false;
            for(int i = 0; i < LATEST_DISCLOSURE_IDS.size(); i++) {
                List<DisclosureItem> items = loadDisclosureItems(i);
                if (LATEST_DISCLOSURE_IDS.get(i) != -1) {
                    disclosureItems.addAll(items);
                } else {
                    initRun = true;
                }
                if(items.size() > 0) {
                    LATEST_DISCLOSURE_IDS.set(i, items.stream().map(DisclosureItem::getDisclosureId).max(Integer::compare).orElse(-1));
                }
            }
            if (!disclosureItems.isEmpty()) {
                processDisclosures(disclosureItems);
                sendNewDisclosures(disclosureItems);
                configSync.saveConfiguration(CACHE_KEY, new DisclosureCache(System.currentTimeMillis(), LATEST_DISCLOSURE_IDS));
            } else if(initRun) {
                configSync.saveConfiguration(CACHE_KEY, new DisclosureCache(System.currentTimeMillis(), LATEST_DISCLOSURE_IDS));
            }
        } catch (IOException | InterruptedException e) {
            log.error("HTTP request to api.news.eu.nasdaq.com failed", e);
        }
    }
}

package com.etsubu.stonksbot.scheduler.omxnordic;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.DisclosureItem;
import com.etsubu.stonksbot.scheduler.Schedulable;
import com.etsubu.stonksbot.scheduler.SchedulerService;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NasdaqNordicDisclosures implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(NasdaqNordicDisclosures.class);
    private static final int OMXH_ID = 0;
    private static final int FIRST_NORTH_ID = 1;
    private static final String[] DISCLOSURE_URL_TEMPLATES = {
            "https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&market=Main%20Market%2C+Helsinki&cnscategory=&company=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicMainMarkets&displayLanguage=fi&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&start=%d&dir=DESC",
            "https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&company=&market=First%20North+Finland&cnscategory=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicFirstNorth&displayLanguage=en&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&start=%d&dir=DESC"
    };
    private final int[] LATEST_DISCLOSURE_IDS = {-1, -1};
    private static final int DELAY_IN_TASK = 120; // 2min'
    private final Gson gson;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;

    public NasdaqNordicDisclosures(SchedulerService schedulerService, ConfigLoader configLoader, EventCore eventCore) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        gson = new Gson();
        log.info("Registering scheduled task {}", getClass().getName());
        // Only request updates between 5am-21pm UTC time and not during the weekends.
        schedulerService.registerTask(this, DELAY_IN_TASK, 5, 21, false);
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
            String url = String.format(DISCLOSURE_URL_TEMPLATES[id], start);
            Optional<String> response = HttpApi.sendGet(url);
            if(response.isEmpty()) {
                return new LinkedList<>();
            }
            Optional<List<DisclosureItem>> items = parseResponse(response.get());
            if(items.isEmpty()) {
                return new LinkedList<>();
            }
            List<DisclosureItem> itemList = items.get();
            if(LATEST_DISCLOSURE_IDS[id] == -1 || itemList.get(itemList.size() - 1).getDisclosureId() < LATEST_DISCLOSURE_IDS[id]) {
                // Found earlier disclosure than the previously latest one
                lastIdFound = true;
            }
            // Collect new disclosures
            newDisclosures.addAll(items.get().stream().filter(x ->
                            Optional.ofNullable(x.getDisclosureId()).map(y -> y > LATEST_DISCLOSURE_IDS[id]).orElse(false) &&
                                    Optional.ofNullable(x.getLanguage()).map(y -> y.equals("fi")).orElse(false))
                    .collect(Collectors.toList()));
            start += itemList.size();
        }
        List<DisclosureItem> items = new ArrayList<>(newDisclosures);
        Collections.sort(items);
        return items;
    }

    @Override
    public void invoke() {
        if (Optional.ofNullable(configLoader.getConfig().getOmxhNews()).map(x -> !Boolean.parseBoolean(x.getEnabled())).orElse(false)) {
            // Not enabled, skip
            return;
        }
        try {
            List<DisclosureItem> disclosureItems = new ArrayList<>();
            for(int i = 0; i < LATEST_DISCLOSURE_IDS.length; i++) {
                List<DisclosureItem> items = loadDisclosureItems(i);
                if (LATEST_DISCLOSURE_IDS[i] != -1) {
                    disclosureItems.addAll(items);
                }
                if(items.size() > 0) {
                    LATEST_DISCLOSURE_IDS[i] = items.stream().map(DisclosureItem::getDisclosureId).max(Integer::compare).orElse(-1);
                }
            }
            if (!disclosureItems.isEmpty()) {
                sendNewDisclosures(disclosureItems);
            }
        } catch (IOException | InterruptedException e) {
            log.error("HTTP request to api.news.eu.nasdaq.com failed", e);
        }
    }
}

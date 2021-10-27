package com.etsubu.stonksbot.scheduler.omxnordic;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ServerConfig;
import com.etsubu.stonksbot.discord.EventCore;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.OmxNewsItem;
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
public class NasdaqOmxNordicNews implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(NasdaqOmxNordicNews.class);
    private static final int DELAY_IN_TASK = 120; // 2min'
    private int latestIdOmxh;
    private int latestIdFirstNorth;
    private final Gson gson;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;

    public NasdaqOmxNordicNews(SchedulerService schedulerService, ConfigLoader configLoader, EventCore eventCore) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        gson = new Gson();
        latestIdOmxh = -1;
        latestIdFirstNorth = -1;
        log.info("Registering scheduled task {}", getClass().getName());
        schedulerService.registerTask(this, DELAY_IN_TASK);
    }

    public Optional<List<OmxNewsItem>> parseResponse(String raw) {
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
                    List<OmxNewsItem> news = new ArrayList<>(items.length());
                    for (int i = 0; i < items.length(); i++) {
                        news.add(gson.fromJson(items.get(i).toString(), OmxNewsItem.class));
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

    private void sendNewsPosts(List<OmxNewsItem> items) {
        log.info("Resolving news attachments");
        items.forEach(OmxNewsItem::resolveAttachments);
        List<Long> channelIds = configLoader.getConfig().getServers().stream().map(ServerConfig::getNewsChannel).filter(Objects::nonNull).collect(Collectors.toList());
        if (!channelIds.isEmpty()) {
            items.forEach(x -> eventCore.sendMessage(channelIds, x.toString(), x.getFiles()));
        }
    }

    private List<OmxNewsItem> listNewsItems(Optional<String> omxhResponse, int latestId) {
        if (omxhResponse.isEmpty()) {
            return new LinkedList<>();
        }
        Optional<List<OmxNewsItem>> items = parseResponse(omxhResponse.get());
        if (items.isPresent()) {
            // Filter only latest news
            return items.get().stream().filter(x ->
                            Optional.ofNullable(x.getDisclosureId()).map(y -> y > latestId).orElse(false) &&
                                    Optional.ofNullable(x.getLanguage()).map(y -> y.equals("fi")).orElse(false))
                    .collect(Collectors.toList());
        } else {
            log.error("Failed to process api.news.eu.nasdaq.com response");
        }
        return new LinkedList<>();
    }

    @Override
    public void invoke() {
        if (Optional.ofNullable(configLoader.getConfig().getOmxhNews()).map(x -> !Boolean.parseBoolean(x.getEnabled())).orElse(false)) {
            // Not enabled, skip
            return;
        }
        try {
            Optional<String> omxhResponse = HttpApi.sendGet("https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&market=Main%20Market%2C+Helsinki&cnscategory=&company=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicMainMarkets&displayLanguage=fi&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&start=0&dir=DESC");
            Optional<String> firstNorthResponse = HttpApi.sendGet("https://api.news.eu.nasdaq.com/news/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&company=&market=First%20North+Finland&cnscategory=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicFirstNorth&displayLanguage=en&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&start=0&dir=DESC");
            List<OmxNewsItem> omxhItems = listNewsItems(omxhResponse, latestIdOmxh);
            List<OmxNewsItem> firstNorthItems = listNewsItems(firstNorthResponse, latestIdFirstNorth);
            List<OmxNewsItem> totalNewsItems = new ArrayList<>(omxhItems.size() + firstNorthItems.size());
            if (latestIdOmxh != -1) {
                totalNewsItems.addAll(omxhItems);
            }
            if (latestIdFirstNorth != -1) {
                totalNewsItems.addAll(firstNorthItems);
            }
            if (omxhItems.size() > 0) {
                latestIdOmxh = omxhItems.stream().map(OmxNewsItem::getDisclosureId).max(Integer::compare).orElse(-1);
            }
            if (firstNorthItems.size() > 0) {
                latestIdFirstNorth = firstNorthItems.stream().map(OmxNewsItem::getDisclosureId).max(Integer::compare).orElse(-1);
            }
            if (totalNewsItems.size() > 0) {
                sendNewsPosts(totalNewsItems);
            }
        } catch (IOException | InterruptedException e) {
            log.error("HTTP request to api.news.eu.nasdaq.com failed", e);
        }
    }
}

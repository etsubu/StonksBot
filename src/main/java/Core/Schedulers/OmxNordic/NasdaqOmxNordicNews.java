package Core.Schedulers.OmxNordic;

import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import Core.Discord.EventCore;
import Core.Discord.ServerChannel;
import Core.HTTP.HttpApi;
import Core.Schedulers.OmxNordic.Model.OmxNewsItem;
import Core.Schedulers.Schedulable;
import Core.Schedulers.SchedulerService;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NasdaqOmxNordicNews implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(NasdaqOmxNordicNews.class);
    private final int DELAY_IN_TASK = 60; // 1min
    private int latestId;
    private final Gson gson;
    private final EventCore eventCore;
    private final ConfigLoader configLoader;

    public NasdaqOmxNordicNews(SchedulerService schedulerService, ConfigLoader configLoader, EventCore eventCore) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        gson = new Gson();
        latestId = -1;
        log.info("Registering scheduled task {}", getClass().getName());
        schedulerService.registerTask(this, DELAY_IN_TASK);

    }

    public Optional<List<OmxNewsItem>> parseResponse(String raw) {
        int begin = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if(begin == -1 || end == -1) {
            log.info("Received invalid data from omx nasdaq nordic {}", raw);
            return Optional.empty();
        }
        try {
            raw = raw.substring(begin, end + 1);
            JSONObject root = new JSONObject(raw);
            if(root.has("results")) {
                JSONObject results = root.getJSONObject("results");
                if(results.has("item")) {
                    JSONArray items = results.getJSONArray("item");
                    List<OmxNewsItem> news = new ArrayList<>(items.length());
                    for (int i = 0; i < items.length(); i++) {
                        news.add(gson.fromJson(items.get(i).toString(), OmxNewsItem.class));
                    }
                    if (news.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(news);
                }
                else {
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
        List<ServerChannel> serverConfigs = configLoader.getConfig().getServers().stream().filter(x -> x.getNewsChannel() != null)
                .map(x -> new ServerChannel(x.getName(), x.getNewsChannel()))
                .collect(Collectors.toList());
        if(!serverConfigs.isEmpty()) {
            items.forEach(x -> {
                eventCore.sendMessage(serverConfigs, x.toString(), x.getFiles());
            });
        }
    }

    @Override
    public void invoke() {
        if(configLoader.getConfig().getOmxhNews() == null ||
                configLoader.getConfig().getOmxhNews().getEnabled() == null ||
                !Boolean.parseBoolean(configLoader.getConfig().getOmxhNews().getEnabled())) {
            // Not enabled, skip
            return;
        }
        try {
            Optional<String> response = HttpApi.sendGet("https://api.news.eu.nasdaq.com/query.action?type=json&showAttachments=true&showCnsSpecific=true&showCompany=true&callback=handleResponse&countResults=false&freeText=&market=Main%20Market%2C+Helsinki&cnscategory=&company=&fromDate=&toDate=&globalGroup=exchangeNotice&globalName=NordicMainMarkets&displayLanguage=fi&language=&timeZone=CET&dateMask=yyyy-MM-dd+HH%3Amm%3Ass&limit=20&start=0&dir=DESC");
            if(response.isEmpty()) {
                log.error("Did not receive response from api.news.eu.nasdaq.com");
            } else {
                Optional<List<OmxNewsItem>> items = parseResponse(response.get());
                if(items.isEmpty()) {
                    log.error("Failed to process api.news.eu.nasdaq.com response");
                } else {
                    List<OmxNewsItem> news = items.get();
                    if(latestId != -1) {
                        // Filter only latest news
                        news = news.stream().filter(x ->
                                Optional.ofNullable(x.getDisclosureId()).map(y -> y > latestId).orElse(false) &&
                                Optional.ofNullable(x.getLanguage()).map(y -> y.equals("fi")).orElse(false))
                                .collect(Collectors.toList());
                        if(news.size() > 0) {
                            sendNewsPosts(news);
                        }
                    }
                    latestId = Optional.ofNullable(news.get(0).getDisclosureId()).orElse(-1);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("HTTP request to api.news.eu.nasdaq.com failed", e);
        }
    }
}

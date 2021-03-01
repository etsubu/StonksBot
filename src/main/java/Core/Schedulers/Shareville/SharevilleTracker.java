package Core.Schedulers.Shareville;

import Core.Configuration.ConfigLoader;
import Core.Configuration.ServerConfig;
import Core.Discord.EventCore;
import Core.HTTP.HttpApi;
import Core.Schedulers.Schedulable;
import Core.Schedulers.SchedulerService;
import Core.Schedulers.Shareville.Model.SharevilleUser;
import Core.Schedulers.Shareville.Model.WallEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduled service that will track a list of shareville users and sends notifications when new transaction happen to
 * a discord channel
 */
@Component
public class SharevilleTracker implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(SharevilleTracker.class);
    private static final String PROFILE_URL_TEMPLATE = "https://www.shareville.fi/api/v1/profiles/%s/stream";
    private static final int DELAY = 300; // 5min
    private final ConfigLoader configLoader;
    private final Map<String, SharevilleUser> profileMap;
    private final EventCore eventCore;

    public SharevilleTracker(ConfigLoader configLoader, SchedulerService schedulerService, EventCore eventCore) {
        this.configLoader = configLoader;
        this.eventCore = eventCore;
        profileMap = new HashMap<>();
        schedulerService.registerTask(this, DELAY);
    }

    private String buildNotification(List<SharevilleUser> users) {
        StringBuilder builder = new StringBuilder("```Shareville tapahtumat:\n");
        boolean content = false;
        for(SharevilleUser user : users) {
            SharevilleUser old = profileMap.get(user.getUrl());
            if(old.getLatestTranscationDate().isPresent()) {
                List<WallEntry> transactions = user.getTransactionAfter(old.getLatestTranscationDate().get());
                if(transactions != null && transactions.size() > 0) {
                    for(WallEntry we : transactions) {
                        if(we.isValid()) {
                            content = true;
                            String name = we.getFirst().getProfile().getName();
                            builder.append("Käyttäjä: ").append(name).append('\n');
                            builder.append("Kohde: ").append(we.getTransaction().getInstrument().getName()).append('\n');
                            builder.append("Tyyppi: ").append(we.getTransaction().getSideAsDescriptive()).append('\n');
                            builder.append("Hinta: ").append(we.getTransaction().getPrice()).append(we.getTransaction().getInstrument().getCurrency()).append('\n');
                            builder.append("---------------");
                        }
                    }
                }
            }
        }
        return content ? builder.append("\n```").toString() : null;
    }

    public void notify(List<ServerConfig> serverConfigs, List<SharevilleUser> sharevilleUsers) {
        for(ServerConfig server : serverConfigs) {
            List<SharevilleUser> changedUsers = new ArrayList<>(sharevilleUsers);
            Long channel = server.getShareville().getSharevilleChannel();
            // Filter only those that the server is tracking
            changedUsers.removeIf(x -> server.getShareville().getSharevilleProfiles().stream().noneMatch(y->x.getUrl().contains("/"+y+"/")));
            changedUsers.removeIf(x -> !profileMap.containsKey(x.getUrl()));
            // Filter profiles that have new transactions
            //changedUsers.removeIf(x -> profileMap.containsKey(x.getUrl())
            //        && x.getLatestTransactionTimestamp().compareTo(profileMap.get(x.getUrl()).getLatestTransactionTimestamp()) >= 0);
            // We now have a list of profiles that the server tracks and that have new transactions
            String notification = buildNotification(changedUsers);
            if(notification != null) {
                log.info("Sent shareville transaction notification");
                eventCore.sendMessage(List.of(channel), notification, null);
            }
        }
    }

    @Override
    public void invoke() {
        if(!Boolean.parseBoolean(configLoader.getConfig().getShareville().getEnabled())) {
            // feature is not enabled;
            return;
        }
        // Filter servers that have configs enabled
        List<ServerConfig> serverConfigs = configLoader.getConfig().getServers()
                .stream()
                .filter(x -> x.getShareville().getSharevilleChannel() != null && x.getShareville().getSharevilleProfiles().size() > 0)
                .collect(Collectors.toList());
        // Collect all profile ids that should be followed
        Set<String> profileIds = serverConfigs.stream()
                .map(ServerConfig::getShareville)
                .map(x -> x.getSharevilleProfiles().stream().map(y -> String.format(PROFILE_URL_TEMPLATE, y)).collect(Collectors.toSet()))
                .collect(HashSet::new, Set::addAll, Set::addAll);
        if(profileIds.isEmpty()) {
            // No profiles to track, skip
            return;
        }
        try {
            // Query profile walls from shareville
            Map<String, String> walls = HttpApi.sendMultipleGet(profileIds);
            // Convert to valid profiles
            List<SharevilleUser> wallProfiles = walls.entrySet().stream().map(x -> {
                try {
                    return new SharevilleUser(x.getValue(), x.getKey());
                } catch (IllegalArgumentException e) {
                    log.error("Failed to map profile page ", e);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            // Remove profiles that are not tracked anymore
            profileMap.entrySet().removeIf(x -> !profileIds.contains(x.getKey()));
            // Filter profiles that have new transactions
            notify(serverConfigs, wallProfiles);
            // Update profiles
            wallProfiles.forEach(x -> profileMap.put(x.getUrl(), x));
        } catch (IOException | InterruptedException e) {
            log.error("Connection error while retrieving profile walls", e);
        }
    }
}

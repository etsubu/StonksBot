package com.etsubu.stonksbot.scheduler.shareville.Model;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ToString
public class SharevilleUser {
    private static final Gson gson = new Gson();
    private final ProfileWall wall;
    private final String url;

    public SharevilleUser(String body, String url) {
        if(body == null) {
            throw new IllegalArgumentException("Body cannot be null");
        }
        this.url = url;
        try {
            wall = gson.fromJson(body, ProfileWall.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Failed to parse json body of wall");
        }
        if(wall == null || wall.getResults() == null) {
            throw new IllegalArgumentException("Profile wall did not contain any entries");
        }
        // Remove invalid times
        wall.getResults().removeIf(x -> x.getTimeInstant().isEmpty());
        // Sort
        wall.getResults().sort(WallEntry::compareTo);
        if(wall.getResults().isEmpty()) {
            throw new IllegalArgumentException("No entries with valid timestamp in profile wall");
        }
    }

    public Optional<Instant> getLatestTranscationDate() {
        return wall.getResults().get(0).getTimeInstant();
    }

    public String getLatestTransactionTimestamp() {
        return wall.getResults().get(0).getTime();
    }

    public List<WallEntry> getTransactionAfter(Instant time) {
        return wall.getResults().stream()
                .filter(x -> x.getTimeInstant().isPresent() && x.getTimeInstant().get().isAfter(time))
                .collect(Collectors.toList());
    }

    public String getUrl() { return url; }
}

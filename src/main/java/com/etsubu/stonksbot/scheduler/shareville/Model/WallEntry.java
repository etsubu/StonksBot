package com.etsubu.stonksbot.scheduler.shareville.Model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Getter
@ToString
public class WallEntry implements Comparable<WallEntry> {
    private static final Logger log = LoggerFactory.getLogger(WallEntry.class);
    @SerializedName("object")
    private final Transaction transaction;
    @SerializedName("created_at")
    private final String time;
    private final EntryFirst first;

    public Optional<Instant> getTimeInstant() {
        if (time == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(time)));
        } catch (Exception e) {
            log.error("Failed to parse iso date time from {}", time);
        }
        return Optional.empty();
    }

    public boolean isValid() {
        return Optional.ofNullable(transaction).map(x -> transaction.isValid()).orElse(false)
                && Optional.ofNullable(first).map(x -> first.isValid()).orElse(false);
    }

    @Override
    public int compareTo(@NotNull WallEntry wallEntry) {
        return wallEntry.time.compareTo(time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WallEntry we = (WallEntry) o;
        return Objects.equals(time, we.time) && Objects.equals(first, we.first) && Objects.equals(transaction, we.transaction);
    }
}

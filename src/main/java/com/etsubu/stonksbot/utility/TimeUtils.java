package com.etsubu.stonksbot.utility;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides commonly used time utility functions
 *
 * @author etsubu
 */
public class TimeUtils {
    private static final ZoneId zone = ZoneId.of("Europe/Helsinki");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter finnishFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String formatEpocSeconds(long epoch) {
        ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), zone);
        return formatter.format(time);
    }

    public static ZonedDateTime parseTime(String time) {
        return ZonedDateTime.parse(time, finnishFormatter);
    }

    public static String currentTime(String time) {
        return finnishFormatter.format(ZonedDateTime.now());
    }
}

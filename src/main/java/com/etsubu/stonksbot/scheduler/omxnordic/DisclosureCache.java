package com.etsubu.stonksbot.scheduler.omxnordic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DisclosureCache {
    private final long timestamp;
    private final int[] latestIds;
}

package com.etsubu.stonksbot.scheduler.omxnordic;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DisclosureCache {
    private final long timestamp;
    private final List<Integer> latestIds;
}

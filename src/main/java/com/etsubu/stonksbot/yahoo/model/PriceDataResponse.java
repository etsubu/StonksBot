package com.etsubu.stonksbot.yahoo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PriceDataResponse {
    private final PriceMeta meta;
    private final List<Integer> timestamps;
    private final List<Long> timestamp;
}

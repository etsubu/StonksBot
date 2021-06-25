package com.etsubu.stonksbot.yahoo.model.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public class PriceMeta {
    private final String currency;
    private final String symbol;
    private final String exhangeName;
    private final String instrumentType;
    private final String firstTradeDate;
    private final String regularMarketTime;
    private final String gmtooffset;
    private final String timezone;
    private final String exchangeTimezoneName;
    private final String regularMarketPrice;
    private final String chartPreviousClose;
    private final String priceHint;
    private final CurrentTradingPeriod currentTradingPeriod;
    private final String dataGranularity;

    public Duration dataGranularityToDuration() {
        if (dataGranularity.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        } else if (dataGranularity.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        } else if (dataGranularity.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        }
        throw new IllegalArgumentException("Failed to parse data granularity " + dataGranularity);
    }
}

package com.etsubu.stonksbot.yahoo.model;

import lombok.Getter;

@Getter
public class PriceMeta {
    private String currency;
    private String symbol;
    private String exhangeName;
    private String instrumentType;
    private String firstTradeDate;
    private String regularMarketTime;
    private String gmtoffset;
    private String timezone;
    private String exhangeTimezoneName;
    private String regularMarketPrice;
    private String chartPreviousClose;
    private String priceHint;

    private String dataGranularity;
    private String range;
}

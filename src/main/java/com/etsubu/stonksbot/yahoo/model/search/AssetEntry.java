package com.etsubu.stonksbot.yahoo.model.search;

import lombok.Getter;

@Getter
public class AssetEntry {
    private String exhange;
    private String shortname;
    private String quoteType;
    private String symbol;
    private String index;
    private String score;
    private String typeDisp;
    private String longname;
    private String isYahooFinance;
}

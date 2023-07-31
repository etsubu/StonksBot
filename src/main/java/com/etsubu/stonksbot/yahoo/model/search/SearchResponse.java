package com.etsubu.stonksbot.yahoo.model.search;

import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse {
    private int count;
    private List<AssetEntry> quotes;
}

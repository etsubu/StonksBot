package com.etsubu.stonksbot.yahoo.model.price;

import lombok.Getter;

import java.util.List;

@Getter
public class PriceQuote {
    private List<String> low;
    private List<String> open;
    private List<String> high;
    private List<String> close;
    private List<String> volume;
}

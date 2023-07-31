package com.etsubu.stonksbot.yahoo.model.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PriceQuote {
    private final List<String> low;
    private final List<String> open;
    private final List<String> high;
    private final List<String> close;
    private final List<String> volume;
}

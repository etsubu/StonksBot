package com.etsubu.stonksbot.yahoo.model.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradingPeriod {
    private final String timezone;
    private final String start;
    private final String end;
    private final String gmtoffset;
}

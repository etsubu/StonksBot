package com.etsubu.stonksbot.yahoo.model.price;

import lombok.Getter;

@Getter
public class TradingPeriod {
    private String timezone;
    private String start;
    private String end;
    private String gmtoffset;
}

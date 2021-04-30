package com.etsubu.stonksbot.yahoo.model.price;

import lombok.Getter;

@Getter
public class CurrentTradingPeriod {
    private TradingPeriod pre;
    private TradingPeriod regular;
    private TradingPeriod post;
}

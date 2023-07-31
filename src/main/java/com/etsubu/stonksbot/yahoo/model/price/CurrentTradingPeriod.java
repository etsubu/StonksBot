package com.etsubu.stonksbot.yahoo.model.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentTradingPeriod {
    private final TradingPeriod pre;
    private final TradingPeriod regular;
    private final TradingPeriod post;
}

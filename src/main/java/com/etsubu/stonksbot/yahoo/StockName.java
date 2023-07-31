package com.etsubu.stonksbot.yahoo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class StockName {
    private final String ticker;
    private final String fullname;

    public StockName(String ticker, String fullname) {
        this.ticker = ticker;
        this.fullname = fullname;
    }
}

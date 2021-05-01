package com.etsubu.stonksbot.yahoo.model.fundament;

import java.util.List;
import java.util.Optional;

public class FundaMeta {
    private final List<String> symbol;
    private final List<String> type;

    public FundaMeta(List<String> symbol, List<String> type) {
        this.symbol = symbol;
        this.type = type;
    }

    public Optional<String> getSymbol() {
        if(symbol != null && !symbol.isEmpty()) {
            return Optional.of(symbol.get(0));
        }
        return Optional.empty();
    }

    public Optional<String> getType() {
        if(type != null && !type.isEmpty()) {
            return Optional.of(type.get(0));
        }
        return Optional.empty();
    }
}

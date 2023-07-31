package com.etsubu.stonksbot.yahoo.model.price;

import java.util.List;
import java.util.Optional;

public class PriceIndicators {
    private final List<PriceQuote> quote;

    public PriceIndicators(List<PriceQuote> quote) {
        this.quote = quote;
    }

    public Optional<PriceQuote> getQuote() {
        if (quote != null && !quote.isEmpty()) {
            return Optional.of(quote.get(0));
        }
        return Optional.empty();
    }
}

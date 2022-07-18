package com.etsubu.stonksbot.scheduler.recommendations.model;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import lombok.ToString;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

public abstract class RecommendationChange {
    private final String isin;

    public RecommendationChange(RecommendationEntry entry) {
        if(entry == null || !entry.isValid()) {
            throw new IllegalArgumentException("Recommendation entry cannot be null nor can its ISIN");
        }
        this.isin = entry.getIsin();
    }

    public String getIsin() { return isin; }

    abstract boolean lightChange();

    public abstract String buildNotificationMessage(AssetPriceIntraInfo currentPrice);
}

package com.etsubu.stonksbot.scheduler.recommendations.model;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.Optional;

public class RemovedRecommendation extends RecommendationChange {
    private final RecommendationEntry last;

    public RemovedRecommendation(RecommendationEntry entry) {
        super(entry);
        this.last = entry;
    }

    @Override
    boolean lightChange() {
        return false;
    }

    @Override
    public String buildNotificationMessage(AssetPriceIntraInfo currentPrice) {
        StringBuilder builder = new StringBuilder();
        Num targetPrice = DecimalNum.valueOf(last.getTarget().replaceAll(",", "."));
        builder.append("```\n(Inderes)\nSeurannan lopetus:");
        builder.append("\nNimi: ").append(last.getName()).append('\n');
        builder.append("Tavoitehinta: ").append(last.getTarget()).append('\n');
        Optional.ofNullable(currentPrice).ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(last.getCurrency())
                .append("\nNousuvara: ")
                .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                .append("%\n"));
        builder.append("Suositus: ").append(last.getRecommendationText()).append('\n');
        builder.append("Riski: ").append(last.getRisk()).append("\n```");
        return builder.toString();
    }
}

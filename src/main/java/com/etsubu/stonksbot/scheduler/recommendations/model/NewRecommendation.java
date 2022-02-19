package com.etsubu.stonksbot.scheduler.recommendations.model;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.Optional;

public class NewRecommendation extends RecommendationChange {
    private final RecommendationEntry initial;

    public NewRecommendation(RecommendationEntry entry) {
        super(entry);
        this.initial = entry;
    }

    @Override
    public String buildNotificationMessage(AssetPriceIntraInfo currentPrice) {
        StringBuilder builder = new StringBuilder();
        Num targetPrice = DecimalNum.valueOf(initial.getTarget().replaceAll(",", "."));
        builder.append("```\n(Inderes)\nSeurannan aloitus:");
        builder.append("\nNimi: ").append(initial.getName()).append('\n');
        builder.append("Tavoitehinta: ").append(initial.getTarget()).append('\n');
        Optional.ofNullable(currentPrice).ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(initial.getCurrency())
                .append("\nNousuvara: ")
                .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                .append("%\n"));
        builder.append("Suositus: ").append(initial.getRecommendationText()).append('\n');
        builder.append("Riski: ").append(initial.getRisk()).append("\n```");
        return builder.toString();
    }
}

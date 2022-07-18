package com.etsubu.stonksbot.scheduler.recommendations.model;

import com.etsubu.stonksbot.inderes.model.RecommendationEntry;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.yahoo.model.AssetPriceIntraInfo;
import lombok.Getter;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.Objects;
import java.util.Optional;

@Getter
public class ChangedRecommendation extends RecommendationChange {
    private final RecommendationEntry from;
    private final RecommendationEntry to;

    public ChangedRecommendation(RecommendationEntry from, RecommendationEntry to) {
        super(from);
        if(to == null || !Objects.equals(from.getIsin(), to.getIsin())) {
            throw new IllegalArgumentException("Changed recommendation must have the same stock for \"from\" and \"to\" states");
        }
        this.from = from;
        this.to = to;
    }


    @Override
    boolean lightChange() {
        return from.isDateOnlyChanged(to);
    }

    @Override
    public String buildNotificationMessage(AssetPriceIntraInfo currentPrice) {
        StringBuilder builder = new StringBuilder();
        Num targetPrice = DecimalNum.valueOf(to.getTarget().replaceAll(",", "."));
        builder.append("```\n(Inderes)\nSuositusmuutos:");
        builder.append("\nNimi: ").append(to.getName()).append('\n');
        builder.append("Tavoitehinta: ").append(from.getTarget()).append(" -> ").append(to.getTarget()).append('\n');
        Optional.ofNullable(currentPrice).ifPresent(x -> builder.append("Nykyinen hinta: ").append(DoubleTools.round(x.getCurrent().toString(), 3)).append(to.getCurrency())
                .append("\nNousuvara: ")
                .append(DoubleTools.round(targetPrice.minus(x.getCurrent()).dividedBy(x.getCurrent()).multipliedBy(DecimalNum.valueOf(100)).toString(), 2))
                .append("%\n"));
        builder.append("Suositus: ").append(from.getRecommendationText()).append(" -> ").append(to.getRecommendationText()).append('\n');
        builder.append("Riski: ").append(from.getRisk()).append(" -> ").append(to.getRisk()).append("\n```");
        return builder.toString();
    }
}

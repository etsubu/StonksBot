package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.utility.NumberTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class TransactionItem extends DisclosureItem {
    private static final Logger log = LoggerFactory.getLogger(TransactionItem.class);
    private static final String TRANSACTION_TYPE_PREFIX = "Liiketoimen luonne: ";
    private static final String VOLUME_PREFIX = "Volyymi: ";
    private static final String AVG_PRICE_PREFIX = "Keskihinta: ";
    protected String content;
    protected String transactionType;
    protected Long volume;
    protected Double avgPrice;
    protected String currency;

    public TransactionItem(DisclosureItem item) {
        super(item.disclosureId, item.categoryId, item.headline, item.language, item.languages, item.company,
                item.cnsCategory, item.messageUrl, item.releaseTime, item.published, item.cnsTypeId, item.attachment,
                item.files);
        if(isTransaction() && messageUrl != null) {
            // Load transaction information
            try {
                Optional<String> content = HttpApi.sendGet(messageUrl);
                if(content.isEmpty()) {
                    log.error("Failed to load disclosure content {}", messageUrl);
                } else {
                    this.content = content.get();
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to load disclosure content {}", messageUrl);
            }
        } else {
            throw new IllegalArgumentException("No message url present or the disclosure is not a transaction");
        }
    }

    public void loadValues() {
        if(content != null) {
            int typeIndex = content.indexOf(TRANSACTION_TYPE_PREFIX);
            int volumeIndex = content.lastIndexOf(VOLUME_PREFIX);
            int avgPriceIndex = content.lastIndexOf(AVG_PRICE_PREFIX);
            transactionType = content.substring(typeIndex + TRANSACTION_TYPE_PREFIX.length(), content.indexOf('<', typeIndex + TRANSACTION_TYPE_PREFIX.length()));
            String volumeStr = content.substring(volumeIndex + VOLUME_PREFIX.length(), content.indexOf(' ', volumeIndex + VOLUME_PREFIX.length()))
                    .replaceAll(" ", "")
                    .trim();
            String avgPriceStr = content.substring(avgPriceIndex + AVG_PRICE_PREFIX.length());
            currency = avgPriceStr.substring(avgPriceStr.indexOf(' ') + 1, avgPriceStr.indexOf('<'));

            avgPriceStr = avgPriceStr.substring(0, avgPriceStr.indexOf(' '))
                    .replaceAll(",", ".")
                    .replaceAll(" ", "")
                    .trim();
            try {
                volume = Long.parseLong(volumeStr);
                avgPrice = Double.parseDouble(avgPriceStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse volume or price '{}', '{}'", volumeStr, avgPriceStr);
            }
        }
    }

    @Override
    public String toString() {
        if (isValid()) {
            StringBuilder builder = new StringBuilder();
            builder.append("**Otsikko**: `")
                    .append(headline)
                    .append("`\n**Yhtiö**: `")
                    .append(company)
                    .append("`\n**Tapahtumalaji**: `")
                    .append(cnsCategory)
                    .append("`\n**Ilmoitus**: ")
                    .append(messageUrl);
            if(volume != null && avgPrice != null && transactionType != null) {
                builder.append("`\n**Toimeksiannon tyyppi**: `")
                        .append(transactionType);
                if(avgPrice == 0) {
                    builder.append("`\n**Volyymi**: `")
                            .append(NumberTools.formatToUserFriendly(volume)).append('`');
                } else {
                    builder.append("`\n**Summa**: `")
                            .append(NumberTools.formatToUserFriendly(DoubleTools.round(avgPrice * volume, 0)))
                            .append(' ')
                            .append(currency).append('`');
                }
            }
            builder.append("\n````");
            return builder.toString();
        } else {
            return "Missing news item info";
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}

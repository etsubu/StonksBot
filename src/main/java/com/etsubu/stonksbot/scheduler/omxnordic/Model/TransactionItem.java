package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.utility.HttpApi;
import com.etsubu.stonksbot.utility.NumberTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class TransactionItem extends DisclosureItem {
    private static final Logger log = LoggerFactory.getLogger(TransactionItem.class);
    private static final String TRANSACTION_TYPE_PREFIX = "Liiketoimen luonne: ";
    private static final String VOLUME_PREFIX = "Volyymi: ";
    private static final String AVG_PRICE_PREFIX = "Keskihinta: ";
    protected String content;
    protected Map<String, List<Transaction>> transactions;

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
        transactions = new HashMap<>();
    }

    private boolean isNumber(String str) {
        try{
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Optional<Transaction> parseTransaction(String content) {
        try {
            int typeIndex = content.indexOf(TRANSACTION_TYPE_PREFIX);
            int volumeIndex = content.lastIndexOf(VOLUME_PREFIX);
            int avgPriceIndex = content.lastIndexOf(AVG_PRICE_PREFIX);
            String transactionType = content.substring(typeIndex + TRANSACTION_TYPE_PREFIX.length(), content.indexOf('<', typeIndex + TRANSACTION_TYPE_PREFIX.length()))
                    .trim().replaceAll("&Auml;", "Ä");
            String volumeStr = content.substring(volumeIndex + VOLUME_PREFIX.length(), content.indexOf('K', volumeIndex + VOLUME_PREFIX.length()))
                    .replaceAll(" ", "")
                    .replaceAll(",", "")
                    .replaceAll(" ", "")
                    .trim();
            String avgPriceStr = content.substring(avgPriceIndex + AVG_PRICE_PREFIX.length());
            String currency = avgPriceStr.substring(avgPriceStr.indexOf(' ') + 1, avgPriceStr.indexOf('<'))
                    .replaceAll("&nbsp;","").replaceAll("&ouml;", "").trim();

            avgPriceStr = avgPriceStr.substring(0, avgPriceStr.indexOf(' '))
                    .replaceAll(",", ".")
                    .replaceAll(" ", "")
                    .replaceAll("&nbsp;","")
                    .replaceAll("&ouml;", "")
                    .trim();
            if(isNumber(currency)) {
                String tmp = currency;
                currency = avgPriceStr;
                avgPriceStr = tmp;
            }

            int volume = Integer.parseInt(volumeStr);
            double avgPrice = Double.parseDouble(avgPriceStr);
            return Optional.of(new Transaction(transactionType, currency, volume, avgPrice));
        } catch (NumberFormatException e) {
            log.error("Failed to parse volume or price", e);
        } catch (Exception e) {
            log.error("Failed to parse transaction = {}", content, e);
        }
        return Optional.empty();
    }

    public void loadValues() {
        String parseContent = content;
        if(content != null) {
            int startIndex = 0;
            while((startIndex = parseContent.indexOf(TRANSACTION_TYPE_PREFIX, startIndex)) != -1) {
                int endIndex = parseContent.indexOf(TRANSACTION_TYPE_PREFIX, startIndex +  TRANSACTION_TYPE_PREFIX.length());
                endIndex = endIndex == -1 ? parseContent.length() : endIndex;
                parseTransaction(parseContent.substring(startIndex, endIndex)).ifPresent(x -> {
                    var transactionsForType = transactions.get(x.key());
                    if(transactionsForType == null) {
                        transactionsForType = new ArrayList<>();
                        transactionsForType.add(x);
                        transactions.put(x.key(), transactionsForType);
                    } else {
                        transactionsForType.add(x);
                    }
                });
                startIndex += TRANSACTION_TYPE_PREFIX.length();
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
            for(var transactionsOfType : transactions.values()) {
                if(!transactionsOfType.isEmpty()) {
                    String type = transactionsOfType.get(0).type();
                    int volume = transactionsOfType.stream().map(Transaction::volume).reduce(0, Integer::sum);
                    String currency = transactionsOfType.get(0).currency();
                    double sum = transactionsOfType.stream().map(Transaction::totalSum).reduce(0D, Double::sum);
                    builder.append("\n**Toimeksiannon tyyppi**: `")
                            .append(type).append("`");
                    if(sum == 0) {
                        builder.append("\n**Volyymi**: `")
                                .append(NumberTools.formatToUserFriendly(volume)).append('`').append('\n');
                    } else {
                        builder.append("\n**Summa**: `")
                                .append(NumberTools.formatToUserFriendly(DoubleTools.round(sum, 0)))
                                .append(' ')
                                .append(currency).append('`').append('\n');
                    }
                }
            }
            builder.append("````");
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

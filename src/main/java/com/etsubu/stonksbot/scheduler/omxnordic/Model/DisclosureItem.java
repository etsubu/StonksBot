package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.HttpApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Getter
@AllArgsConstructor
public class DisclosureItem implements Comparable<DisclosureItem> {
    private static final Logger log = LoggerFactory.getLogger(DisclosureItem.class);
    private static final Set<Integer> TRANSACTION_CATEGORY_IDS = Set.of(66, 231);
    protected final Integer disclosureId;
    protected final Integer categoryId;
    protected final String headline;
    protected final String language;
    protected final List<String> languages;
    protected final String company;
    protected final String cnsCategory;
    protected final String messageUrl;
    protected final String releaseTime;
    protected final String published;
    protected final String cnsTypeId;
    protected final List<OmxNewsAttachment> attachment;
    protected List<AttachmentFile> files;

    public boolean isValid() {
        return headline != null && company != null && cnsCategory != null && messageUrl != null;
    }

    public void resolveAttachments() {
        if (attachment != null) {
            files = new ArrayList<>(attachment.size());
            for (OmxNewsAttachment att : attachment) {
                log.info("Downloading attachment {}", att.getFileName());
                try {
                    Optional<byte[]> rawFile = HttpApi.downloadFile(att.getAttachmentUrl());
                    if (rawFile.isPresent()) {
                        files.add(new AttachmentFile(rawFile.get(), att.getFileName()));
                    } else {
                        log.error("Failed to download attachment {}", att.getFileName());
                    }
                } catch (IOException | InterruptedException e) {
                    log.error("Failed to download news attachment", e);
                }
            }
        }
    }

    public boolean isTransaction() {
        return Optional.ofNullable(categoryId).map(TRANSACTION_CATEGORY_IDS::contains).orElse(false);
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
                    .append(messageUrl)
                    .append('\n');
            return builder.toString();
        } else {
            return "Missing news item info";
        }
    }

    @Override
    public int compareTo(@NotNull DisclosureItem o) {
        // Descending order
        return o.disclosureId.compareTo(disclosureId);
    }

    @Override
    public int hashCode() {
        return disclosureId != null ? disclosureId.hashCode() : 1001;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()) {
            return false;
        }
        return Objects.equals(((DisclosureItem) (o)).getDisclosureId(), getDisclosureId());
    }
}

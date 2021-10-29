package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.HttpApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class OmxNewsItem implements Comparable<OmxNewsItem> {
    private static final Logger log = LoggerFactory.getLogger(OmxNewsItem.class);
    private final Integer disclosureId;
    private final Integer categoryId;
    private final String headline;
    private final String language;
    private final List<String> languages;
    private final String company;
    private final String cnsCategory;
    private final String messageUrl;
    private final String releaseTime;
    private final String published;
    private final String cnsTypeId;
    private final List<OmxNewsAttachment> attachment;
    private List<AttachmentFile> files;

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
                    .append("\n````");
            return builder.toString();
        } else {
            return "Missing news item info";
        }
    }

    @Override
    public int compareTo(@NotNull OmxNewsItem o) {
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
        return Objects.equals(((OmxNewsItem) (o)).getDisclosureId(), getDisclosureId());
    }
}

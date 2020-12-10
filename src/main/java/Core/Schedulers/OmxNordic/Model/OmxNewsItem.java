package Core.Schedulers.OmxNordic.Model;

import Core.HTTP.HttpApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class OmxNewsItem {
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
        if(attachment != null) {
            files = new ArrayList<>(attachment.size());
            for(OmxNewsAttachment att : attachment) {
                log.info("Downloading attachment {}", att.getFileName());
                try {
                    Optional<byte[]> rawFile = HttpApi.downloadFile(att.getAttachmentUrl());
                    if(rawFile.isPresent()) {
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
        if(isValid()) {
            StringBuilder builder = new StringBuilder();
            builder.append("```Otsikko: ")
                    .append(headline)
                    .append("\nYhtiö: ")
                    .append(company)
                    .append("\nTapahtumalaji: ")
                    .append(cnsCategory)
                    .append("\nIlmoitus: ")
                    .append(messageUrl)
                    .append("\n```");
            return builder.toString();
        } else {
            return "Missing news item info";
        }
    }
}

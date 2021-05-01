package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OmxNewsAttachment {
    private final String mimetype;
    private final String fileName;
    private final String attachmentUrl;
}

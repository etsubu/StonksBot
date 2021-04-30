package com.etsubu.stonksbot.scheduler.OmxNordic.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OmxNewsAttachment {
    private final String mimetype;
    private final String fileName;
    private final String attachmentUrl;
}

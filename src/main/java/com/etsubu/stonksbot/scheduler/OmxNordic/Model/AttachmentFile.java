package com.etsubu.stonksbot.scheduler.OmxNordic.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttachmentFile {
    private final byte[] file;
    private final String filename;
}

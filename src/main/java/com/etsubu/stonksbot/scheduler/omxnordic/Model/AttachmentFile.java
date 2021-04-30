package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.TypeUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AttachmentFile {
    private final byte[] file;
    private final String filename;

    public String getFilename() { return filename; }

    public byte[] getFile() {
        return TypeUtils.cloneByteArray(file);
    }
}

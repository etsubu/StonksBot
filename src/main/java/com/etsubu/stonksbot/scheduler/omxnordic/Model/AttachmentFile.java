package com.etsubu.stonksbot.scheduler.omxnordic.Model;

import com.etsubu.stonksbot.utility.TypeUtils;

public class AttachmentFile {
    private final byte[] file;
    private final String filename;

    public AttachmentFile(byte[] file, String filename) {
        this.file = TypeUtils.cloneByteArray(file);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getFile() {
        return TypeUtils.cloneByteArray(file);
    }
}

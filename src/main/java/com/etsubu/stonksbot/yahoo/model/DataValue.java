package com.etsubu.stonksbot.yahoo.model;

import java.util.Optional;

public class DataValue {
    private final String raw;
    private final String fmt;
    private final String longFmt;

    public DataValue(String raw, String fmt, String longFmt) {
        this.raw = raw;
        this.fmt = fmt;
        this.longFmt = longFmt;
    }

    public String getRaw() {
        return raw;
    }

    public String getFmt() {
        return fmt;
    }

    public Optional<String> getLongFmt() {
        return Optional.ofNullable(longFmt);
    }
}

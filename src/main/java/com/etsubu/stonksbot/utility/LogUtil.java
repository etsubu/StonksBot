package com.etsubu.stonksbot.utility;

public class LogUtil {
    private static final int LOG_LINE_MAX_LENGTH = 512;

    public static String sanitizeLogValue(String msg) {
        if(msg == null || msg.isEmpty()) {
            return msg;
        }
        msg = msg.replaceAll("\n", "");
        if(msg.length() > LOG_LINE_MAX_LENGTH) {
            return msg.substring(0, LOG_LINE_MAX_LENGTH - 3) + "...";
        }
        return msg;
    }
}

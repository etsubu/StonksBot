package com.etsubu.stonksbot.utility;

public class MessageUtils {
    public static String cleanMessage(String message) {
        if(message == null || message.length() < 2000) {
            return message;
        }
        return message.substring(0, 1996) + "...";
    }
}

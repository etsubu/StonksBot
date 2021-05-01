package com.etsubu.stonksbot.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerChannel {
    private final String serverName;
    private final String channelName;
}

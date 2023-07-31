package com.etsubu.stonksbot.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommandConfig {
    private List<String> allowedGroups;
}

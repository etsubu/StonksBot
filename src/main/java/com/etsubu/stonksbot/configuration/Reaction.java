package com.etsubu.stonksbot.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Reaction {
    private Pattern pattern;
    private String message;
    private String react;

    public Reaction() {
    }

    public void buildPattern() {
        if (message != null) {
            pattern = Pattern.compile(message);
        }
    }
}

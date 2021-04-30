package com.etsubu.stonksbot.scheduler.Shareville.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Profile {
    private final Long id;
    private final String slug;
    private final String name;

    public boolean isValid() {
        return name != null;
    }
}

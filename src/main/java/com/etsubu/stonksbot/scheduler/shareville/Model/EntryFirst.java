package com.etsubu.stonksbot.scheduler.shareville.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
public class EntryFirst {
    private final Long id;
    private final Profile profile;

    public boolean isValid() {
        return Optional.ofNullable(profile).map(Profile::isValid).orElse(false);
    }
}

package com.etsubu.stonksbot.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class EventConfig {
    private Boolean enabled;
    private String title;
    private List<String> allowedGroupIds;
    private Integer max;
}

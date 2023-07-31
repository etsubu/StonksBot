package com.etsubu.stonksbot.yahoo.model.fundament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FundamentEntry {
    private FundaMeta meta;
    private List<String> timestamp;
    /* Name of this field changes and thus has to be set manually instead of relying on gson */
    private List<FundaValue> value;
}

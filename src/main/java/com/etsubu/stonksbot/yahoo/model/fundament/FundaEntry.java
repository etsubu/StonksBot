package com.etsubu.stonksbot.yahoo.model.fundament;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FundaEntry {
    private FundaMeta meta;
    private List<String> timestamp;
    private FundaValue value;
}

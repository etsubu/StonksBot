package com.etsubu.stonksbot.yahoo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssetProfile {
    private final String address1;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;
    private final String phone;
    private final String website;
    private final String industry;
    private final String sector;
    private final String longBusinessSummary;
    private final String fullTimeEmployees;
}

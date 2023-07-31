package com.etsubu.stonksbot.yahoo.model;

import com.etsubu.stonksbot.yahoo.StockName;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class DataResponse {
    private StockName name;
    private final CalendarEarnings calendarEvents;
    private final DefaultKeyStatistics defaultKeyStatistics;
    private final AssetProfile assetProfile;

    public Optional<CalendarEarnings> getCalendarEvents() {
        return Optional.ofNullable(calendarEvents);
    }

    public Optional<DefaultKeyStatistics> getDefaultKeyStatistics() {
        return Optional.ofNullable(defaultKeyStatistics);
    }

    public Optional<AssetProfile> getAssetProfile() {
        return Optional.ofNullable(assetProfile);
    }

    public StockName getName() {
        return name;
    }

    public void setName(StockName name) {
        this.name = name;
    }
}

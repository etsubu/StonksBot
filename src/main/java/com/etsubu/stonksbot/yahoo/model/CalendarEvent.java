package com.etsubu.stonksbot.yahoo.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Container for a single calendar event and its estimates.
 *
 * @author etsubu
 */
public class CalendarEvent {
    public final List<DataValue> earningsDate;
    public final DataValue earningsAverage;
    public final DataValue earningsLow;
    public final DataValue earningsHigh;
    public final DataValue revenueAverage;
    public final DataValue revenueLow;
    public final DataValue revenueHigh;

    public CalendarEvent(List<DataValue> earningsDate,
                         DataValue earningsAverage,
                         DataValue earningsLow,
                         DataValue earningsHigh,
                         DataValue revenueAverage,
                         DataValue revenueLow,
                         DataValue revenueHigh) {
        this.earningsDate = earningsDate;
        this.earningsAverage = earningsAverage;
        this.earningsLow = earningsLow;
        this.earningsHigh = earningsHigh;
        this.revenueAverage = revenueAverage;
        this.revenueLow = revenueLow;
        this.revenueHigh = revenueHigh;
    }

    public Optional<List<DataValue>> earningsDate() {
        return Optional.ofNullable(earningsDate).map(Collections::unmodifiableList);
    }

    public Optional<DataValue> getEarningsAverage() {
        if (earningsAverage != null && earningsAverage.getRaw() != null) {
            return Optional.of(earningsAverage);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getEarningsLow() {
        if (earningsLow != null && earningsLow.getRaw() != null) {
            return Optional.of(earningsLow);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getEarningsHigh() {
        if (earningsHigh != null && earningsHigh.getRaw() != null) {
            return Optional.of(earningsHigh);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueAverage() {
        if (revenueAverage != null && revenueAverage.getRaw() != null) {
            return Optional.of(revenueAverage);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueLow() {
        if (revenueLow != null && revenueLow.getRaw() != null) {
            return Optional.of(revenueLow);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueHigh() {
        if (revenueHigh != null && revenueHigh.getRaw() != null) {
            return Optional.of(revenueHigh);
        }
        return Optional.empty();
    }
}

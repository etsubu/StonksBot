package com.etsubu.stonksbot.scheduler.shareville.Model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Instrument {
    private final Long id;
    @SerializedName("instrument_id")
    private final String instrumentId;
    private final String name;
    private final String slug;
    private final String currency;
    @SerializedName("instrument_group_type")
    private final String instrumentGroupType;

    public boolean isValid() {
        return id != null && instrumentId != null && name != null && currency != null;
    }
}

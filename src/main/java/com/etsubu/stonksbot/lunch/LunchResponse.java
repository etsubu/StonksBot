package com.etsubu.stonksbot.lunch;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LunchResponse {
    @SerializedName("LunchMenu")
    private final LunchMenu menu;

}

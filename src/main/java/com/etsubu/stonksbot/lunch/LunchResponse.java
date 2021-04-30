package com.etsubu.stonksbot.lunch;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class LunchResponse {
    @SerializedName("LunchMenu")
    private LunchMenu menu;

}

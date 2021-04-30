package com.etsubu.stonksbot.lunch;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MealOption {
    @SerializedName("SortOrder")
    private final String sortOrder;
    @SerializedName("Name")
    private final String name;
    @SerializedName("Meals")
    private final List<MealComponents> meals;
}

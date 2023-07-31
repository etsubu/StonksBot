package com.etsubu.stonksbot.lunch;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MealComponents {
    @SerializedName("Name")
    private final String name;
    @SerializedName("RecipeId")
    private final String recipeId;
    @SerializedName("Diets")
    private final List<String> diets;
    @SerializedName("Nutrients")
    private final Object nutrients;
    @SerializedName("IconUrl")
    private final String iconUrl;
}

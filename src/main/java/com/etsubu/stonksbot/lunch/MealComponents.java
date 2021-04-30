package com.etsubu.stonksbot.lunch;

import lombok.Getter;

import java.util.List;

@Getter
public class MealComponents {
    private String Name;
    private String RecipeId;
    private List<String> Diets;
    private Object Nutrients;
    private String IconUrl;
}

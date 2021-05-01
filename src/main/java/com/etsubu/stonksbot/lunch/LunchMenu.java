package com.etsubu.stonksbot.lunch;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LunchMenu {
    private String restaurantName;
    private String DayOfWeek;
    private String Date;
    private List<MealOption> SetMenus;
}

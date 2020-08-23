package Core.Lunch;

import lombok.Getter;

import java.util.List;

@Getter
public class MealOption {
    private String SortOrder;
    private String Name;
    private List<MealComponents> Meals;
}

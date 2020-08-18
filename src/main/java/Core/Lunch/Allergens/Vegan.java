package Core.Lunch.Allergens;

public class Vegan extends Allergen {
    public static final Vegan VEGAN = new Vegan();

    private Vegan() {
        super("Veg", "Vegan");
    }
}

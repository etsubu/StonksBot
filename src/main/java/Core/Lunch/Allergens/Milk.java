package Core.Lunch.Allergens;

public class Milk extends Allergen {
    public static final Milk MILK = new Milk();

    private Milk() {
        super("M", "No Milk");
    }
}

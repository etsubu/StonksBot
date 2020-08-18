package Core.Lunch.Allergens;

public class LowLactose extends Allergen {
    public static final LowLactose LOW_LACTOSE = new LowLactose();

    private LowLactose() {
        super("VL", "Low lactose");
    }
}

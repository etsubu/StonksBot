package Core.Lunch.Allergens;

public class Lactose extends Allergen {
    public static final Lactose LACTOSE = new Lactose();

    private Lactose() {
        super("L", "No lactose");
    }
}

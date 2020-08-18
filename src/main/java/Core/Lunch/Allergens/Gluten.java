package Core.Lunch.Allergens;

public class Gluten extends Allergen {
    public static final Gluten GLUTEN = new Gluten();

    private Gluten() {
        super("G", "Gluten free");
    }
}

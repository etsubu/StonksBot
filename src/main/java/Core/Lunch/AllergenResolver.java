package Core.Lunch;

import Core.Lunch.Allergens.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AllergenResolver {
    public static final AllergenResolver ALLERGEN_RESOLVER = new AllergenResolver();

    private final Map<String, Allergen> allergenMap;

    private AllergenResolver() {
        this.allergenMap = new HashMap<>();
        addAllergen(Gluten.GLUTEN);
        addAllergen(Milk.MILK);
        addAllergen(Lactose.LACTOSE);
        addAllergen(LowLactose.LOW_LACTOSE);
        addAllergen(Vegan.VEGAN);
    }

    public Optional<Allergen> resolveAllergen(String acronym) {
        return Optional.ofNullable(allergenMap.get(acronym));
    }

    private void addAllergen(Allergen allergen) {
        this.allergenMap.put(allergen.getAcronym(), allergen);
    }
}

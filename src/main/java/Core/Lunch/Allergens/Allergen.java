package Core.Lunch.Allergens;

public abstract class Allergen {
    protected final String acronym;
    protected final String name;

    public Allergen(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public String getAcronym() { return acronym; }

    public String getName() { return name; }
}

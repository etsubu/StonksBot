package Core.Lunch;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class LunchOption {
    @SerializedName("Name")
    private final String name;
    @SerializedName("Components")
    private final List<String> components;

    public LunchOption(String name, List<String> components) {
        this.name = name;
        this.components = components;
    }

    public String getName() { return name; }

    public List<String> getComponents() { return Collections.unmodifiableList(components); }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append('\n');
        components.forEach(x -> builder.append(x).append('\n'));
        return builder.toString();
    }
}

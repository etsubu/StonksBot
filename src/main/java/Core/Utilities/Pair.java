package Core.Utilities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Pair<X,Y> {
    public final X first;
    public final Y second;

    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }
}

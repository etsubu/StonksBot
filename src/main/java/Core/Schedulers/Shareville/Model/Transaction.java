package Core.Schedulers.Shareville.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
public class Transaction {
    private final Long id;
    private final Instrument instrument;
    private final String price;
    private final Integer side;

    public boolean isValid() {
        return side != null && price != null && Optional.ofNullable(instrument).map(Instrument::isValid).orElse(false);
    }

    public String getSideAsDescriptive() {
        if(side == null) {
            return null;
        }
        switch (side) {
            case 1: return "Osto";
            case 2: return "Myynti";
            default: return "Tuntematon toimeksianto";
        }
    }
}

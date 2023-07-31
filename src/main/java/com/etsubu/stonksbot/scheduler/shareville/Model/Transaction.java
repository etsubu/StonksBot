package com.etsubu.stonksbot.scheduler.shareville.Model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Transaction {
    private final Long id;
    private final Instrument instrument;
    private final String price;
    private final Integer side;

    public boolean isValid() {
        return side != null && price != null && Optional.ofNullable(instrument).map(Instrument::isValid).orElse(false);
    }

    public String getSideAsDescriptive() {
        if (side == null) {
            return null;
        }
        return switch (side) {
            case 1 -> "Osto";
            case 2 -> "Myynti";
            default -> "Tuntematon toimeksianto";
        };
    }
}

package Core.YahooAPI.DataStructures.Fundamentals;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

public class FundaMeta {
    private List<String> symbol;
    private List<String> type;

    public Optional<String> getSymbol() {
        if(symbol != null && !symbol.isEmpty()) {
            return Optional.of(symbol.get(0));
        }
        return Optional.empty();
    }

    public Optional<String> getType() {
        if(type != null && !type.isEmpty()) {
            return Optional.of(type.get(0));
        }
        return Optional.empty();
    }
}

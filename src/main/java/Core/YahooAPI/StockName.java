package Core.YahooAPI;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StockName {
    private final String ticker;
    private final String fullname;

    public StockName(String ticker, String fullname) {
        this.ticker = ticker;
        this.fullname = fullname;
    }

    @Override
    public String toString() {
        return ticker.toUpperCase() + ":" + fullname;
    }
}

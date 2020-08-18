package Core.YahooAPI.DataStructures;

import java.util.Optional;

public class CalendarEarnings {
    private final int maxAge;
    private final CalendarEvent earnings;
    private final DataValue exDividendDate;
    private final DataValue dividendDate;

    public CalendarEarnings(int maxAge, CalendarEvent earnings, DataValue exDividendDate, DataValue dividendDate) {
        this.maxAge = maxAge;
        this.earnings = earnings;
        this.exDividendDate = exDividendDate;
        this.dividendDate = dividendDate;
    }

    public int getMaxAge() { return maxAge; }

    public Optional<CalendarEvent> getEarnings() {
        return Optional.ofNullable(earnings);
    }

    public Optional<DataValue> getExDividendDate() {
        if(exDividendDate != null && exDividendDate.getRaw() != null) {
            return Optional.of(exDividendDate);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getDividendDate() {
        if(dividendDate != null && dividendDate.getRaw() != null) {
            return Optional.of(dividendDate);
        }
        return Optional.empty();
    }

}

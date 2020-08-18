package Core.YahooAPI.DataStructures;

import java.util.Optional;

public class CalendarEarnings {
    private final int maxAge;
    private final CalendarEvent earnings;
    public final DataValue exDividendDate;
    public final DataValue dividendDate;

    public CalendarEarnings(int maxAge, CalendarEvent earnings, DataValue exDividendDate, DataValue dividendDate) {
        this.maxAge = maxAge;
        this.earnings = earnings;
        this.exDividendDate = exDividendDate;
        this.dividendDate = dividendDate;
    }

    public int getMaxAge() { return maxAge; }

    public Optional<CalendarEvent> getEarnings() { return Optional.ofNullable(earnings); }
}

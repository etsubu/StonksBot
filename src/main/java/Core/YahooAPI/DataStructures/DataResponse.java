package Core.YahooAPI.DataStructures;

import Core.YahooAPI.StockName;

import java.util.Optional;

public class DataResponse {
    private StockName name;
    private final CalendarEarnings calendarEvents;
    private final DefaultKeyStatistics defaultKeyStatistics;

    public DataResponse(CalendarEarnings calendarEvents, DefaultKeyStatistics defaultKeyStatistics) {
        this.calendarEvents = calendarEvents;
        this.defaultKeyStatistics = defaultKeyStatistics;
    }

    public Optional<CalendarEarnings> getCalendarEvents() { return Optional.ofNullable(calendarEvents); }

    public Optional<DefaultKeyStatistics> getDefaultKeyStatistics() { return Optional.ofNullable(defaultKeyStatistics); }

    public StockName getName() { return name; }

    public void setName(StockName name) { this.name = name; }
}

package Core.YahooAPI.DataStructures;

public enum DataType {
    DEFAULT_KEY_STATISTICS("defaultKeyStatistics"),
    CALENDAR_EVENTS("calendarEvents");

    private final String name;

    DataType(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}

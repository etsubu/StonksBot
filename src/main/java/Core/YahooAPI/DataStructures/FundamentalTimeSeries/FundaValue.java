package Core.YahooAPI.DataStructures.FundamentalTimeSeries;

import Core.YahooAPI.DataStructures.DataValue;

import java.util.Optional;

public class FundaValue {
    private String dataId;
    private String asOfDate;
    private String periodType;
    private String currency;
    private DataValue reportedValue;

    public Optional<String> getDataId() { return Optional.ofNullable(dataId); }

    public Optional<String> getAsOfDate() { return Optional.ofNullable(asOfDate); }

    public Optional<String> getPeriodType() { return Optional.ofNullable(periodType); }

    public Optional<String> getCurrency() { return Optional.ofNullable(currency); }

    public Optional<DataValue> getReportedValue() { return Optional.ofNullable(reportedValue); }
}

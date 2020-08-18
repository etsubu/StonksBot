package Core.YahooAPI.DataStructures.PriceChart.PriceMeta;

import lombok.Getter;

import java.time.Duration;

@Getter
public class PriceMeta {
    private String currency;
    private String symbol;
    private String exhangeName;
    private String instrumentType;
    private String firstTradeDate;
    private String regularMarketTime;
    private String gmtooffset;
    private String timezone;
    private String exchangeTimezoneName;
    private String regularMarketPrice;
    private String chartPreviousClose;
    private String priceHint;
    private CurrentTradingPeriod currentTradingPeriod;
    private String dataGranularity;

    public Duration dataGranularityToDuration() {
        if(dataGranularity.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        } else if(dataGranularity.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        } else if(dataGranularity.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(dataGranularity.substring(0, dataGranularity.length() - 1)));
        }
        throw new IllegalArgumentException("Failed to parse data granularity " + dataGranularity);
    }
}

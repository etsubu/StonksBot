package Core.YahooAPI.DataStructures.PriceChart.PriceMeta;

import lombok.Getter;

@Getter
public class TradingPeriod {
    private String timezone;
    private String start;
    private String end;
    private String gmtoffset;
}

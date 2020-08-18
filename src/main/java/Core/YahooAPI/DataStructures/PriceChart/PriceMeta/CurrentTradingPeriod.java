package Core.YahooAPI.DataStructures.PriceChart.PriceMeta;

import lombok.Getter;

@Getter
public class CurrentTradingPeriod {
    private TradingPeriod pre;
    private TradingPeriod regular;
    private TradingPeriod post;
}

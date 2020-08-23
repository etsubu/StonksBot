package Core.YahooAPI.DataStructures.PriceChart.PriceMeta;

import Core.YahooAPI.StockName;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.PrecisionNum;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class ChartResult {
    private static final Gson gson = new Gson();
    private PriceMeta meta;
    private List<String> timestamp;
    private PriceIndicators indicators;

    public static ChartResult buildChartResultFromJson(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject data = root.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
        return gson.fromJson(data.toString(), ChartResult.class);
    }

    public BarSeries getBarSeries() throws IOException {
        BarSeries series = new BaseBarSeries(meta.getSymbol());
        ZoneId zoneId = ZoneId.of(meta.getExchangeTimezoneName());
        PriceQuote quote = indicators.getQuote().get();
        List<String> open = quote.getOpen();
        List<String> high = quote.getHigh();
        List<String> low = quote.getLow();
        List<String> close = quote.getClose();
        List<String> volume = quote.getVolume();
        for(int i = 0; i < timestamp.size(); i++) {
            buildCandle(series, zoneId, open, high, low, close, volume, i);
        }
        return series;
    }

    public BarSeries getIntraBarSeries() throws IOException {
        BarSeries series = new BaseBarSeries(meta.getSymbol());
        ZoneId zoneId = ZoneId.of(meta.getExchangeTimezoneName());
        PriceQuote quote = indicators.getQuote().get();
        List<String> open = quote.getOpen();
        List<String> high = quote.getHigh();
        List<String> low = quote.getLow();
        List<String> close = quote.getClose();
        List<String> volume = quote.getVolume();
        long intraStartTime = Long.parseLong(meta.getCurrentTradingPeriod().getRegular().getStart());
        long intraEndTime = Long.parseLong(meta.getCurrentTradingPeriod().getRegular().getEnd());
        for(int i = 0; i < timestamp.size(); i++) {
            long currentTime = Long.parseLong(timestamp.get(i));
            if(currentTime < intraStartTime) {
                continue;
            }
            if(currentTime > intraEndTime) {
                break;
            }
            buildCandle(series, zoneId, open, high, low, close, volume, i);
        }
        return series;
    }

    private void buildCandle(BarSeries series, ZoneId zoneId, List<String> open, List<String> high, List<String> low, List<String> close, List<String> volume, int i) throws IOException {
        Instant ins = Instant.ofEpochSecond(Long.parseLong(timestamp.get(i)));
        ZonedDateTime z = ZonedDateTime.ofInstant(ins, zoneId);
        PrecisionNum v = PrecisionNum.valueOf(Optional.ofNullable(volume.get(i)).orElse("0"));
        PrecisionNum o = PrecisionNum.valueOf(open.get(i));
        PrecisionNum h = PrecisionNum.valueOf(high.get(i));
        PrecisionNum l = PrecisionNum.valueOf(low.get(i));
        PrecisionNum c = PrecisionNum.valueOf(close.get(i));
        Bar bar = new BaseBar(meta.dataGranularityToDuration(), z, o, h, l, c, v, PrecisionNum.valueOf(1));
        series.addBar(bar);
    }
}

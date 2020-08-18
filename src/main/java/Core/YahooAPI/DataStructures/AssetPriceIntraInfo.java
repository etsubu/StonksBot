package Core.YahooAPI.DataStructures;

import lombok.Getter;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

@Getter
public class AssetPriceIntraInfo {
    private String symbol;
    private Num open;
    private Num high;
    private Num low;
    private Num current;
    private Num volume;
    private Num priceVolume;
    private Num vwap;

    public AssetPriceIntraInfo(BarSeries series) {
        symbol = series.getName();
        open = series.getFirstBar().getOpenPrice();
        high = series.getFirstBar().getHighPrice();
        low = series.getFirstBar().getLowPrice();
        current = series.getLastBar().getClosePrice();
        volume = PrecisionNum.valueOf(0);
        vwap = PrecisionNum.valueOf(0);
        for(Bar b : series.getBarData()) {
            if(b.getHighPrice().isGreaterThan(high)) {
                high = b.getHighPrice();
            }
            if(b.getLowPrice().isLessThan(low)) {
                low = b.getLowPrice();
            }
            Num PV = (b.getLowPrice().plus(b.getHighPrice()).plus(b.getClosePrice())).dividedBy(PrecisionNum.valueOf(3)).multipliedBy(b.getVolume());
            vwap = vwap.plus(PV);
            volume = volume.plus(b.getVolume());
        }
        priceVolume = vwap;
        vwap = vwap.dividedBy(volume);
    }

    public double round(Num value, int decimals) {
        double exp = Math.pow(10, decimals);
        double d = value.doubleValue() * exp;
        return  Math.round(d) / exp;
    }

    public String formatLong(Num value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(value.longValue());
    }

    public Num getChangePercent() {
        return ((current.minus(open)).dividedBy(open)).multipliedBy(PrecisionNum.valueOf(100));
    }

    @Override
    public String toString() {
        return "```\n" + symbol + "\n"
                + "Price: " + round(current, 3) + "\n"
                + "Change: " + round(getChangePercent(), 2) + "%\n"
                + "High: " + round(high, 3) + "\n"
                + "Low: " + round(low, 3) + "\n"
                + "VWAP: " + round(vwap, 2) + "\n"
                + "Volume: " + formatLong(volume) + "\n"
                + "Price Volume: " + formatLong(priceVolume) + "\n```";
    }
}

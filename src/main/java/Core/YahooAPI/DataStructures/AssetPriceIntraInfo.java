package Core.YahooAPI.DataStructures;

import lombok.Getter;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

@Getter
public class AssetPriceIntraInfo {
    private final String symbol;
    private final Num previousClose;
    private final Num open;
    private final Num high;
    private final Num low;
    private final Num current;
    private final Num volume;

    public AssetPriceIntraInfo(BarSeries series) {
        symbol = series.getName();
        previousClose = series.getFirstBar().getClosePrice();
        System.out.println(previousClose.toString());
        open = series.getLastBar().getOpenPrice();
        high = series.getLastBar().getHighPrice();
        low = series.getLastBar().getLowPrice();
        current = series.getLastBar().getClosePrice();
        volume = series.getLastBar().getVolume();
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
        return ((current.minus(previousClose)).dividedBy(previousClose)).multipliedBy(PrecisionNum.valueOf(100));
    }

    @Override
    public String toString() {
        return "```\n" + symbol + "\n"
                + "Price: " + round(current, 3) + "\n"
                + "Change: " + round(getChangePercent(), 2) + "%\n"
                + "Open: " + round(open, 2) + "\n"
                + "High: " + round(high, 3) + "\n"
                + "Low: " + round(low, 3) + "\n"
                + "Volume: " + formatLong(volume) + "\n```";
    }
}

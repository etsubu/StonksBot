package com.etsubu.stonksbot.yahoo.model;

import com.etsubu.stonksbot.yahoo.StockName;
import lombok.Getter;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

@Getter
public class AssetPriceIntraInfo {
    private final StockName name;
    private final Num previousClose;
    private final Num open;
    private final Num high;
    private final Num low;
    private final Num current;
    private final Num volume;

    public AssetPriceIntraInfo(BarSeries series, StockName name) {

        this.name = name;
        previousClose = series.getFirstBar().getClosePrice();
        open = series.getLastBar().getOpenPrice();
        high = series.getLastBar().getHighPrice();
        low = series.getLastBar().getLowPrice();
        current = series.getLastBar().getClosePrice();
        volume = series.getLastBar().getVolume();
    }

    public double round(Num value, int decimals) {
        double exp = Math.pow(10, decimals);
        double d = value.doubleValue() * exp;
        return Math.round(d) / exp;
    }

    public String formatLong(Num value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(value.longValue());
    }

    public Num getChangePercent() {
        return ((current.minus(previousClose)).dividedBy(previousClose)).multipliedBy(DecimalNum.valueOf(100));
    }

    @Override
    public String toString() {
        return "```\n" + name.getFullname() + " - " + name.getTicker() + "\n"
                + "Price: " + round(current, 3) + "\n"
                + "Change: " + round(getChangePercent(), 2) + "%\n"
                + "Open: " + round(open, 2) + "\n"
                + "High: " + round(high, 3) + "\n"
                + "Low: " + round(low, 3) + "\n"
                + "Volume: " + formatLong(volume) + "\n```";
    }
}

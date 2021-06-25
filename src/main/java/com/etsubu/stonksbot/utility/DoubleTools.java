package com.etsubu.stonksbot.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Common methods for working with floating point numbers
 *
 * @author etsubu
 */
public class DoubleTools {
    private static final Logger log = LoggerFactory.getLogger(DoubleTools.class);

    public static double round(String value, int accuracy) {
        try {
            double exp = Math.pow(10, accuracy);
            return Math.round(Double.parseDouble(value) * exp) / exp;
        } catch (NumberFormatException e) {
            log.error("Failed to round number '{}'", value, e);
        }
        return Double.NaN;
    }

    public static double round(double value, int accuracy) {
        double exp = Math.pow(10, accuracy);
        return Math.round(value * exp) / exp;
    }

    public static double round(double value) {
        return round(value, 2);
    }

    public static String roundToFormat(double value) {
        return String.format("%.02f", round(value, 2));
    }

    public static double roundNumberToPercent(String value) {
        try {
            return round(Double.parseDouble(value) * 100);
        } catch (NumberFormatException e) {
            log.error("Failed to round number '{}'", value, e);
        }
        return Double.NaN;
    }

    public static String formatLong(String value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
        if (!value.contains(".")) {
            return formatter.format(Long.parseLong(value));
        }
        return formatter.format(Double.parseDouble(value));
    }
}

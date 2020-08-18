package Core.Utilities;

import org.ta4j.core.num.Num;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class DoubleTools {
	public static double round(double value, int accuracy) {
		double exp = Math.pow(10, accuracy);
		return Math.round(value * exp) / exp;
	}
	
	public static double round(double value) {
		return round(value, 2);
	}

	public static String formatLong(String value) {
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);
		if(!value.contains(".")) {
			return formatter.format(Long.parseLong(value));
		}
		return formatter.format(Double.parseDouble(value));
	}
}

package Core.Utilities;

public class DoubleTools {
	public static double round(double value, int accuracy) {
		double exp = Math.pow(10, accuracy);
		return Math.round(value * exp) / exp;
	}
	
	public static double round(double value) {
		return round(value, 2);
	}
}

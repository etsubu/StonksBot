package Core.Utilities;

public class Statistics {
    public static double variance(double[] prices) {
        if(prices == null || prices.length <= 1) {
            return 0;
        }
        double mean = Statistics.mean(prices);
        double variance = 0;
        for(double d : prices) {
            double diff = mean - d;
            variance += diff * diff; // pow 2
        }
        return variance / (prices.length - 1);
    }

    public static double mean(double[] prices) {
        if(prices == null || prices.length == 0) {
            return 0;
        }
        double mean = 0;
        for(double d : prices) {
            mean += d;
        }
        return mean / prices.length;
    }

    public static double stdev(double[] prices) {
        return Math.sqrt(Statistics.variance(prices));
    }

    public static double correlation(double[] x, double[] y) {
        if(x == null || y == null || x.length == 0 || x.length != y.length) {
            return -2;
        }
        double xMean = mean(x);
        double yMean = mean(y);
        double diffSum = 0, squaredDiffX = 0, squaredDiffY = 0;
        for(int i = 0; i < x.length; i++) {
            diffSum += (xMean - x[i]) * (yMean - y[i]);
            squaredDiffX += (xMean - x[i]) * (xMean - x[i]);
            squaredDiffY += (yMean - y[i]) * (yMean - y[i]);
        }
        return diffSum / Math.sqrt(squaredDiffX * squaredDiffY);
    }
}

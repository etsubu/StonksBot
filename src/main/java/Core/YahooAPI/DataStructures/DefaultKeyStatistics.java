package Core.YahooAPI.DataStructures;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class DefaultKeyStatistics {
    private int maxAge;
    private DataValue priceHint;
    private DataValue forwardPE;
    private DataValue profitMargins;
    private DataValue floatShares;
    private DataValue sharesOutstanding;
    private DataValue sharesShort;
    private DataValue sharesShortPriorMonth;
    private DataValue dateShortInterest;
    private DataValue sharesPercentSharesOut;
    private DataValue heldPercentInsiders;
    private DataValue heldPercentInsiderInstitutions;
    private DataValue shortRatio;
    private DataValue shortPercentOfFloat;
    private DataValue beta;
    private DataValue bookValue;
    private DataValue priteToBook;

    public Optional<Double> getShortRateChange() {
        if(sharesShort == null || sharesShortPriorMonth == null) {
            return Optional.empty();
        }
        String sharesShortRaw = sharesShort.getRaw();
        String sharesShortPriorMonthRaw = sharesShortPriorMonth.getRaw();
        if(sharesShortPriorMonthRaw == null || sharesShortRaw == null){
            return Optional.empty();
        }
        try {
            BigDecimal sharesShort = new BigDecimal(sharesShortRaw);
            BigDecimal sharesShortPriorMonth = new BigDecimal(sharesShortPriorMonthRaw);
            BigDecimal shortPressureChange = (sharesShort.subtract(sharesShortPriorMonth)).divide(sharesShortPriorMonth, 4, RoundingMode.HALF_UP);
            return Optional.of(shortPressureChange.doubleValue());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public int getMaxAge() { return maxAge; }

    public DataValue getPriceHint() { return priceHint; }

    public DataValue getForwardPE() { return forwardPE; }

    public DataValue getProfitMargins() { return profitMargins; }

    public DataValue getFloatShares() { return floatShares; }

    public DataValue getSharesOutstanding() { return sharesOutstanding; }

    public DataValue getSharesShort() { return sharesOutstanding; }

    public DataValue getSharesShortPriorMonth() { return sharesOutstanding; }

    public DataValue getDateShortInterest() { return dateShortInterest; }

    public DataValue getSharesPercentSharesOut() { return sharesPercentSharesOut; }

    public DataValue getHeldPercentInsiders() { return heldPercentInsiders; }

    public DataValue getHeldPercentInsiderInstitutions() { return heldPercentInsiderInstitutions; }

    public DataValue getShortRatio() { return shortRatio; }

    public DataValue getShortPercentOfFloat() { return shortPercentOfFloat; }

    public DataValue getBeta() { return beta; }

    public DataValue getBookValue() { return bookValue; }

    public DataValue getPriteToBook() { return priteToBook; }
}

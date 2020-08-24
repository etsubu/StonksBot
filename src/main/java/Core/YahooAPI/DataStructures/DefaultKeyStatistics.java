package Core.YahooAPI.DataStructures;

import com.google.gson.annotations.SerializedName;

import javax.xml.crypto.Data;
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
    private DataValue heldPercentInstitutions;
    private DataValue shortRatio;
    private DataValue shortPercentOfFloat;
    private DataValue beta;
    private DataValue bookValue;
    private DataValue priceToBook;
    private DataValue enterpriseToEbitda;
    @SerializedName("52WeekChange")
    private DataValue priceChange;

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

    private Optional<String> getValue(DataValue value) {
        if(value != null && value.getRaw() != null) {
            return Optional.of(value.getRaw());
        }
        return Optional.empty();
    }

    public int getMaxAge() { return maxAge; }

    public Optional<String> getPriceHint() { return getValue(priceHint); }

    public Optional<String> getForwardPE() { return getValue(forwardPE); }

    public Optional<String> getProfitMargins() { return getValue(profitMargins); }

    public Optional<String> getFloatShares() { return getValue(floatShares); }

    public Optional<String> getSharesOutstanding() { return getValue(sharesOutstanding); }

    public Optional<String> getSharesShort() { return getValue(sharesShort); }

    public Optional<String> getSharesShortPriorMonth() { return getValue(sharesShortPriorMonth); }

    public Optional<String> getDateShortInterest() { return getValue(dateShortInterest); }

    public Optional<String> getSharesPercentSharesOut() { return getValue(sharesPercentSharesOut); }

    public Optional<String> getHeldPercentInsiders() { return getValue(heldPercentInsiders); }

    public Optional<String> getHeldPercentInstitutions() { return getValue(heldPercentInstitutions); }

    public Optional<String> getShortRatio() { return getValue(shortRatio); }

    public Optional<String> getShortPercentOfFloat() { return getValue(shortPercentOfFloat); }

    public Optional<String> getBeta() { return getValue(beta); }

    public Optional<String> getBookValue() { return getValue(bookValue); }

    public Optional<String> getPriceToBook() { return getValue(priceToBook); }

    public Optional<String> getEvEbitda() { return getValue(enterpriseToEbitda); }

    public Optional<String> getPriceChange() { return getValue(priceChange); }
}

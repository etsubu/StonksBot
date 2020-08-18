package Core.YahooAPI.DataStructures;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {
 *   "quoteSummary": {
 *     "result": [
 *       {
 *         "calendarEvents": {
 *           "maxAge": 1,
 *           "earnings": {
 *             "earningsDate": [
 *               {
 *                 "raw": 1602547200,
 *                 "fmt": "2020-10-13"
 *               }
 *             ],
 *             "earningsAverage": {
 *               "raw": 1.82,
 *               "fmt": "1.82"
 *             },
 *             "earningsLow": {
 *               "raw": 1.23,
 *               "fmt": "1.23"
 *             },
 *             "earningsHigh": {
 *               "raw": 2.33,
 *               "fmt": "2.33"
 *             },
 *             "revenueAverage": {
 *               "raw": 27373900000,
 *               "fmt": "27.37B",
 *               "longFmt": "27,373,900,000"
 *             },
 *             "revenueLow": {
 *               "raw": 26038500000,
 *               "fmt": "26.04B",
 *               "longFmt": "26,038,500,000"
 *             },
 *             "revenueHigh": {
 *               "raw": 28416000000,
 *               "fmt": "28.42B",
 *               "longFmt": "28,416,000,000"
 *             }
 *           },
 *           "exDividendDate": {
 *             "raw": 1593648000,
 *             "fmt": "2020-07-02"
 *           },
 *           "dividendDate": {
 *             "raw": 1596153600,
 *             "fmt": "2020-07-31"
 *           }
 *         }
 *       }
 *     ],
 *     "error": null
 *   }
 * }
 */
public class CalendarEvent {
    public final List<DataValue> earningsDate;
    public final DataValue earningsAverage;
    public final DataValue earningsLow;
    public final DataValue earningsHigh;
    public final DataValue revenueAverage;
    public final DataValue revenueLow;
    public final DataValue revenueHigh;

    public CalendarEvent(List<DataValue> earningsDate,
                         DataValue earningsAverage,
                         DataValue earningsLow,
                         DataValue earningsHigh,
                         DataValue revenueAverage,
                         DataValue revenueLow,
                         DataValue revenueHigh) {
        this.earningsDate = earningsDate;
        this.earningsAverage = earningsAverage;
        this.earningsLow = earningsLow;
        this.earningsHigh = earningsHigh;
        this.revenueAverage = revenueAverage;
        this.revenueLow = revenueLow;
        this.revenueHigh = revenueHigh;
    }

    public Optional<List<DataValue>> earningsDate() {
        return Optional.ofNullable(earningsDate).map(Collections::unmodifiableList);
    }

    public Optional<DataValue> getEarningsAverage() {
        if(earningsAverage != null && earningsAverage.getRaw() != null) {
            return Optional.of(earningsAverage);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getEarningsLow() {
        if(earningsLow != null && earningsLow.getRaw() != null) {
            return Optional.of(earningsLow);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getEarningsHigh() {
        if(earningsHigh != null && earningsHigh.getRaw() != null) {
            return Optional.of(earningsHigh);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueAverage() {
        if(revenueAverage != null && revenueAverage.getRaw() != null) {
            return Optional.of(revenueAverage);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueLow() {
        if(revenueLow != null && revenueLow.getRaw() != null) {
            return Optional.of(revenueLow);
        }
        return Optional.empty();
    }

    public Optional<DataValue> getRevenueHigh() {
        if(revenueHigh != null && revenueHigh.getRaw() != null) {
            return Optional.of(revenueHigh);
        }
        return Optional.empty();
    }
}

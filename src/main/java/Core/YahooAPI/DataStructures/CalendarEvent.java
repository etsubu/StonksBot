package Core.YahooAPI.DataStructures;

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
        return Optional.ofNullable(earningsAverage);
    }

    public Optional<DataValue> getEarningsLow() {
        return Optional.ofNullable(earningsLow);
    }

    public Optional<DataValue> getEarningsHigh() {
        return Optional.ofNullable(earningsHigh);
    }

    public Optional<DataValue> getRevenueAverage() {
        return Optional.ofNullable(revenueAverage);
    }

    public Optional<DataValue> getRevenueLow() {
        return Optional.ofNullable(revenueLow);
    }

    public Optional<DataValue> getRevenueHigh() {
        return Optional.ofNullable(revenueHigh);
    }
}

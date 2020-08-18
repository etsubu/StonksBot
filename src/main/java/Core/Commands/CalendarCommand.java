package Core.Commands;

import Core.Utilities.DoubleTools;
import Core.Utilities.TimeUtils;
import Core.YahooAPI.DataStructures.CalendarEarnings;
import Core.YahooAPI.DataStructures.CalendarEvent;
import Core.YahooAPI.DataStructures.DataResponse;
import Core.YahooAPI.DataStructures.GeneralResponse;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CalendarCommand extends Command{
    private static final Logger log = LoggerFactory.getLogger(CalendarCommand.class);
    private final YahooConnectorImpl yahooConnector;
    /**
     * Initializes Command
     */
    public CalendarCommand(YahooConnectorImpl yahooConnector) {
        super("calendar");
        this.yahooConnector = yahooConnector;
        log.info("Initialized calendar command");
    }

    private String buildResponse(CalendarEarnings earnings) {
        StringBuilder builder = new StringBuilder();
        CalendarEvent event = earnings.getEarnings().get();
        builder.append("Earnings date: ")
                .append(TimeUtils.formatEpocSeconds(Long.parseLong(event.earningsDate().get().get(0).getRaw())))
                .append('\n');
        event.getEarningsAverage().ifPresent(x -> builder.append("Forecast EPS avg: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        event.getEarningsHigh().ifPresent(x -> builder.append("Forecast EPS high: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        event.getEarningsLow().ifPresent(x -> builder.append("Forecast EPS low: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        event.getRevenueAverage().ifPresent(x -> builder.append("Forecast Revenue avg: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        event.getRevenueHigh().ifPresent(x -> builder.append("Forecast Revenue high: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        event.getRevenueLow().ifPresent(x -> builder.append("Forecast Revenue low: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        earnings.getExDividendDate().ifPresent(x -> builder.append("Previous dividend date: ").append(TimeUtils.formatEpocSeconds(Long.parseLong(x.getRaw()))).append('\n'));
        earnings.getDividendDate().ifPresent(x -> builder.append("Next dividend date: ").append(TimeUtils.formatEpocSeconds(Long.parseLong(x.getRaw()))).append('\n'));
        return builder.toString();
    }

    @Override
    public CommandResult execute(String command) {
        try {
            Optional<DataResponse> response = yahooConnector.queryData(command, YahooConnectorImpl.CALENDAR_EVENTS);
            if(response.isPresent() && response.get().getCalendarEvents().isPresent()) {
                CalendarEarnings event = response.get().getCalendarEvents().get();
                return new CommandResult(buildResponse(event), true);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve calendar events for ticker", e);
        }
        return new CommandResult("Failed to retrieve calendar events", false);
    }

    @Override
    public String help() {
        return null;
    }
}

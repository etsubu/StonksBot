package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.utility.DoubleTools;
import com.etsubu.stonksbot.utility.TimeUtils;
import com.etsubu.stonksbot.yahoo.model.CalendarEarnings;
import com.etsubu.stonksbot.yahoo.model.CalendarEvent;
import com.etsubu.stonksbot.yahoo.model.DataResponse;
import com.etsubu.stonksbot.yahoo.YahooConnectorImpl;
import com.etsubu.stonksbot.yahoo.StockName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class CalendarCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CalendarCommand.class);
    private final YahooConnectorImpl yahooConnector;
    /**
     * Initializes Command
     */
    public CalendarCommand(YahooConnectorImpl yahooConnector, ConfigLoader configLoader) {
        super(List.of("calendar", "kalenteri", "c"), configLoader, true);
        this.yahooConnector = yahooConnector;
        log.info("Initialized calendar command");
    }

    private String buildResponse(CalendarEarnings earnings, StockName name) {
        StringBuilder builder = new StringBuilder();
        builder.append("```");
        builder.append(name.getFullname()).append(" - ").append(name.getTicker()).append('\n');
        if(earnings.getEarnings().isPresent()) {
            CalendarEvent event = earnings.getEarnings().get();
            if(event.earningsDate().isPresent() && event.earningsDate().get().size() > 0) {
                builder.append("Earnings date: ")
                        .append(TimeUtils.formatEpocSeconds(Long.parseLong(event.earningsDate().get().get(0).getRaw())))
                        .append('\n');
            }
            event.getEarningsAverage().ifPresent(x -> builder.append("Forecast EPS avg: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
            event.getEarningsHigh().ifPresent(x -> builder.append("Forecast EPS high: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
            event.getEarningsLow().ifPresent(x -> builder.append("Forecast EPS low: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
            event.getRevenueAverage().ifPresent(x -> builder.append("Forecast Revenue avg: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
            event.getRevenueHigh().ifPresent(x -> builder.append("Forecast Revenue high: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
            event.getRevenueLow().ifPresent(x -> builder.append("Forecast Revenue low: ").append(DoubleTools.formatLong(x.getRaw())).append('\n'));
        }
        earnings.getExDividendDate().ifPresent(x -> builder.append("Previous dividend date: ").append(TimeUtils.formatEpocSeconds(Long.parseLong(x.getRaw()))).append('\n'));
        earnings.getDividendDate().ifPresent(x -> builder.append("Next dividend date: ").append(TimeUtils.formatEpocSeconds(Long.parseLong(x.getRaw()))).append('\n'));
        builder.append("```");
        return builder.toString();
    }

    @Override
    public CommandResult exec(String command) {
        if(command.isBlank()) {
            return new CommandResult("You need to specify stock name to query, see !help price", false);
        }
        try {
            Optional<DataResponse> response = yahooConnector.queryData(command, YahooConnectorImpl.CALENDAR_EVENTS);
            if(response.isPresent() && response.get().getCalendarEvents().isPresent()) {
                CalendarEarnings event = response.get().getCalendarEvents().get();
                return new CommandResult(buildResponse(event, response.get().getName()), true);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve calendar events for ticker", e);
        }
        return new CommandResult("Failed to retrieve calendar events", false);
    }

    @Override
    public String help() {
        return "Lists upcoming calendar events such as dividends, earnings date, and analyst forecasts if available\n"
                + "Usage: !" + String.join("/", super.names) + " [stockname/ticker]\n"
                + "Example: !calendar msft";
    }
}

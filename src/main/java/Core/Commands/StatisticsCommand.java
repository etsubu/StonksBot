package Core.Commands;

import Core.Utilities.DoubleTools;
import Core.YahooAPI.DataStructures.DataResponse;
import Core.YahooAPI.DataStructures.DefaultKeyStatistics;
import Core.YahooAPI.StockName;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class StatisticsCommand extends Command{
    private static final Logger log = LoggerFactory.getLogger(StatisticsCommand.class);
    private final YahooConnectorImpl yahooConnector;
    /**
     * Initializes Command
     */
    public StatisticsCommand(YahooConnectorImpl yahooConnector) {
        super(List.of("stats", "tilastot", "s"));
        this.yahooConnector = yahooConnector;
    }

    private CommandResult buildResponse(DefaultKeyStatistics statistics, StockName name) {
        StringBuilder builder = new StringBuilder();
        builder.append("```\n").append(name.getFullname()).append(" - ").append(name.getTicker()).append('\n');
        statistics.getEvEbitda().ifPresent(x -> builder.append("EV/EBITDA: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getEvRevenue().ifPresent(x -> builder.append("EV/Revenue: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getForwardPE().ifPresent(x -> builder.append("Forward P/E: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getPegRatio().ifPresent(x -> builder.append("PEG: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getPriceToBook().ifPresent(x -> builder.append("P/B: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getBeta().ifPresent(x -> builder.append("Beta: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getTrailingEps().ifPresent(x -> builder.append("Trailing EPS: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getForwardEps().ifPresent(x -> builder.append("Forward EPS: ").append(DoubleTools.round(x, 2)).append('\n'));
        statistics.getForecastedEpsChange().ifPresent(x -> builder.append("Forecasted EPS Growth: ").append(DoubleTools.round(x * 100)).append("%\n"));
        statistics.getProfitMargins().ifPresent(x -> builder.append("Profit margin: ").append(DoubleTools.roundNumberToPercent(x)).append("%\n"));
        statistics.getShortPercentOfFloat().ifPresent(x -> builder.append("Short percent of float: ").append(DoubleTools.roundNumberToPercent(x)).append("%\n"));
        statistics.getShortRateChange().ifPresent(x -> builder.append("Recent change in short ratio: ").append(DoubleTools.round(x * 100)).append("%\n"));
        statistics.getHeldPercentInsiders().ifPresent(x -> builder.append("Held by insiders: ").append(DoubleTools.roundNumberToPercent(x)).append("%\n"));
        statistics.getHeldPercentInstitutions().ifPresent(x -> builder.append("Held by institutions: ").append(DoubleTools.roundNumberToPercent(x)).append("%\n"));
        statistics.getPriceChange().ifPresent(x -> builder.append("52 week price change: ").append(DoubleTools.roundNumberToPercent(x)).append("%\n"));
        builder.append("```");
        return new CommandResult(builder.toString(), true);
    }

    @Override
    public CommandResult execute(String command) {
        log.info("Requesting statistics for {}", command);
        try {
            Optional<DataResponse> response = yahooConnector.queryData(command, YahooConnectorImpl.DEFAULT_STATISTICS);
            if(response.isPresent() && response.get().getDefaultKeyStatistics().isPresent()) {
                log.info("Received statistics for {}", response.get().getName().toString());
                DefaultKeyStatistics statistics = response.get().getDefaultKeyStatistics().get();
                return buildResponse(statistics, response.get().getName());
            }
            return new CommandResult("Failed to retrieve key statistics for the requested stock", false);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve key statistics for requested stock '{}'", command, e);
            return new CommandResult("Failed to retrieve key statistics for the requested stock", false);
        }
    }

    @Override
    public String help() {
        return "Displays key statistics about the given stock\nUsage: !" + String.join("/", super.names) +" [stockname/ticker]\nExample: !stats msft";
    }
}

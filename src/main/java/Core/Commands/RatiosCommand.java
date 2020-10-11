package Core.Commands;

import Core.Utilities.DoubleTools;
import Core.YahooAPI.DataStructures.DataValue;
import Core.YahooAPI.DataStructures.FundamentalTimeSeries.FundamentEntry;
import Core.YahooAPI.YahooConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class RatiosCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(RatiosCommand.class);
    private static final String ENTERPRISE_VALUE = "trailingEnterpriseValue";
    private static final String PRICE_TO_SALES = "trailingPsRatio";
    private static final String EBIT = "trailingEBIT";
    private static final String FREE_CASH_FLOW = "trailingFreeCashFlow";
    private static final String EBITDA = "trailingEBITDA";

    private final YahooConnectorImpl yahooConnector;

    public RatiosCommand(YahooConnectorImpl yahooConnector) {
        super("ratios");
        this.yahooConnector = yahooConnector;
    }

    private CommandResult buildResponse(Map<String, FundamentEntry> entries) {
        StringBuilder response = new StringBuilder("```\n");
        Optional.ofNullable(entries.get(PRICE_TO_SALES)).ifPresent(x -> response.append("P/S: ").append(x.getValue().get(0).getReportedValue().get().getRaw()).append('\n'));
        entries.get(ENTERPRISE_VALUE).getValue().get(0).getReportedValue().ifPresent(x -> System.out.println(x.getRaw()));
        if(entries.containsKey(ENTERPRISE_VALUE)) {
            Optional<DataValue> ev = entries.get(ENTERPRISE_VALUE).getValue().get(0).getReportedValue();
            System.out.println(entries.get(ENTERPRISE_VALUE).getValue());
            System.out.println(ev.get().getRaw() + " " + ev.get().getFmt());
            double enterpriseValue = Double.parseDouble(ev.get().getRaw());
            Optional.ofNullable(entries.get(FREE_CASH_FLOW)).ifPresent(x -> response.append("FCF Yield: ")
                    .append(DoubleTools.round(Double.parseDouble(x.getValue().get(0).getReportedValue().get().getRaw()) / enterpriseValue * 100, 2))
                    .append("%\n"));
            return new CommandResult(response.append("\n```").toString(), true);
        } else {
            return new CommandResult("Did not receive all the info from yahoo finance", false);
        }
    }

    @Override
    public CommandResult execute(String command) {
        log.info("Requesting statistics for {}", command);
        try {
            Map<String, FundamentEntry> response = yahooConnector.queryFundamentTimeSeries(command, ENTERPRISE_VALUE, PRICE_TO_SALES, EBIT, FREE_CASH_FLOW, EBITDA);
            return buildResponse(response);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to retrieve key statistics for requested stock '{}'", command, e);
            return new CommandResult("Failed to retrieve key statistics for the requested stock", false);
        }
    }

    @Override
    public String help() {
        return null;
    }
}

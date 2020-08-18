package Core.Commands;

import Core.Lunch.LunchDay;
import Core.Lunch.LunchList;
import Core.Lunch.LunchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class Lunch extends Command{
    private static final Logger log = LoggerFactory.getLogger(Lunch.class);
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");
    private final LunchQuery query;

    public Lunch() {
        super("lunch");
        // Temporary hard coding. Moving this to config file later
        this.query = new LunchQuery();
    }

    private int getDayToQuery() {
        ZonedDateTime time = ZonedDateTime.now(TIMEZONE);
        int day = time.getDayOfMonth();
        System.out.println(time.getHour());
        if(time.getHour() >= 18) {
            // Use tomorrows lunch list
            day = time.plusDays(1).getDayOfMonth();
        }
        return day;
    }

    private String formatLunches(List<LunchList> lunches) {
        int day = getDayToQuery();
        StringBuilder builder = new StringBuilder();
        for(LunchList list : lunches) {
            builder.append("**")
                    .append(list.getRestaurantName())
                    .append("**").append('\n');
            List<LunchDay> lunchList = list.getLunchDays(day);
            if(!lunchList.isEmpty()) {
                builder.append(lunchList.get(0).getDate().toLocalDate()).append('\n');
            }
            for(LunchDay lunch : lunchList) {
                lunch.getLunchOptions().forEach(x -> {
                    builder.append(x.getName()).append('\n');
                    x.getComponents().forEach(y -> builder.append('\t').append(y).append('\n'));
                });
            }
            builder.append('\n');
        }
        return builder.toString();
    }
    @Override
    public CommandResult execute(String command) {
        log.info("Executing " + command);
        try {
            List<LunchList> lunches = query.getLunchList(getDayToQuery());
            return new CommandResult(formatLunches(lunches), true);
        } catch (IOException | InterruptedException e) {
            log.error("Core.Lunch query failed ", e);
            return new CommandResult("Failed to query lunch " + e.getMessage(), false);
        }
    }

    @Override
    public String help() {
        return "Returns the current days lunch list. Usage !lunch";
    }
}

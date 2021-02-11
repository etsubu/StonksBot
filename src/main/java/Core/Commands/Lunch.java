package Core.Commands;

import Core.Configuration.ConfigLoader;
import Core.Lunch.LunchMenu;
import Core.Lunch.LunchQuery;
import Core.Lunch.MealComponents;
import Core.Lunch.MealOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Retrieves lunch list from "semma" for university of jyväskylä. Piato and Maija restaurants are displayed
 * @author etsubu
 */
@Component
public class Lunch extends Command{
    private static final Logger log = LoggerFactory.getLogger(Lunch.class);
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");
    private final LunchQuery query;

    public Lunch(ConfigLoader configLoader) {
        super(List.of("lunch", "lounas"), configLoader, true);
        // Temporary hard coding. Moving this to config file later
        this.query = new LunchQuery();
    }

    private String formatLunches(List<LunchMenu> menus) {
        if(Optional.ofNullable(menus).map(List::isEmpty).orElse(true)) {
            // Empty lunch list
            return "No lunch list available. Better cook your own food. I recommend chili";
        }
        StringBuilder builder = new StringBuilder();
        for(LunchMenu menu : menus) {
            builder.append("**")
                    .append(menu.getRestaurantName())
                    .append("**")
                    .append('\n')
                    .append(menu.getDayOfWeek())
                    .append(' ')
                    .append(menu.getDate())
                    .append('\n');
            for(MealOption meal : menu.getSetMenus()) {
                builder.append(meal.getName()).append('\n');
                for(MealComponents component : meal.getMeals()) {
                    builder.append('\t').append(component.getName()).append(' ')
                            .append(String.join(", ", component.getDiets()))
                            .append('\n');
                }
            }
            builder.append('\n');
        }
        return builder.toString();
    }
    @Override
    public CommandResult exec(String command) {
        log.info("Executing " + command);
        try {
            List<LunchMenu> lunches = query.queryLunchList();
            return new CommandResult(formatLunches(lunches), true);
        } catch (IOException | InterruptedException e) {
            log.error("Lunch query failed ", e);
            return new CommandResult("Failed to query lunch " + e.getMessage(), false);
        }
    }

    @Override
    public String help() {
        return "Returns the current days lunch list. \nUsage !" + String.join("/", super.names);
    }
}

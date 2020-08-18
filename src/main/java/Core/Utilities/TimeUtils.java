package Core.Utilities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final ZoneId zone = ZoneId.of("Europe/Helsinki");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String formatEpocSeconds(long epoch) {
        ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), zone);
        return formatter.format(time);
    }
}

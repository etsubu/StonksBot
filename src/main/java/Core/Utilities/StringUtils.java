package Core.Utilities;

import java.util.Optional;
import java.util.regex.Pattern;

public class StringUtils {
    private static final String REGEX = "^[0-9]+$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static boolean isNumeric(String str) {
        return Optional.ofNullable(str).map(x -> PATTERN.matcher(x).matches()).orElse(false);
    }
}

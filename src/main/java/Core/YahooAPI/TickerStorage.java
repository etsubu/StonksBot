package Core.YahooAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TickerStorage {
    private static final Logger log = LoggerFactory.getLogger(TickerStorage.class);
    private final Map<String, String> tickerMap;
    private final Path tickerFile;

    public TickerStorage() {
        tickerMap = new HashMap<>();
        tickerFile = Path.of("tickers.txt");
        try {
            init();
        } catch (IOException e) {
            log.error("Failed to initialize tickers.txt", e);
        }
    }

    private void init() throws IOException {
        if(Files.exists(Path.of("tickers.txt"))) {
            Files.readAllLines(tickerFile).forEach(x -> {
                String[] shortcut = x.split(":");
                if(shortcut.length == 2) {
                    tickerMap.put(shortcut[0], shortcut[1]);
                }
            });
        } else {
            Files.createFile(tickerFile);
        }
    }

    public void setShortcut(String shortcut, String ticker) {
        synchronized (tickerMap) {
            if(!tickerMap.containsKey(shortcut)) {
                tickerMap.put(shortcut.toLowerCase(), ticker.toUpperCase());
                try {
                    Files.writeString(tickerFile, shortcut.toLowerCase() + ":" + ticker.toUpperCase() + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    log.error("Failed to store ticker shortcut to file", e);
                }
            }
        }
    }

    public Optional<String> findTicker(String shortcut) {
        synchronized (tickerMap) {
            return Optional.ofNullable(tickerMap.get(shortcut));
        }
    }
}

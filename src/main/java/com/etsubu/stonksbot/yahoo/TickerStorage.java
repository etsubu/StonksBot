package com.etsubu.stonksbot.yahoo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Component for caching stock names and tickers to avoid having to perform same queries
 *
 * @author etsubu
 */
@Component
public class TickerStorage {
    private static final Logger log = LoggerFactory.getLogger(TickerStorage.class);
    private final Map<String, StockName> tickerMap;
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
        if (Files.exists(Path.of("tickers.txt"))) {
            List<String> lines = Files.readAllLines(tickerFile).stream().filter(x -> x.split(":").length == 3).collect(Collectors.toList());
            lines.forEach(x -> {
                String[] shortcut = x.split(":");
                tickerMap.put(shortcut[0], new StockName(shortcut[1], shortcut[2]));
            });
            Files.writeString(tickerFile, String.join("\n", lines) + "\n");
            log.info("Writing ticker storage {} lines", lines.size());
        } else {
            Files.createFile(tickerFile);
        }
    }

    public void setShortcut(String shortcut, StockName name) {
        synchronized (tickerMap) {
            if (!tickerMap.containsKey(shortcut)) {
                tickerMap.put(shortcut.toLowerCase(), name);
                try {
                    Files.writeString(tickerFile, shortcut.toLowerCase() + ":" + name.getTicker() + ":" + name.getFullname() + "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    log.error("Failed to store ticker shortcut to file", e);
                }
            }
        }
    }

    public Optional<StockName> findTicker(String shortcut) {
        synchronized (tickerMap) {
            return Optional.ofNullable(tickerMap.get(shortcut.toLowerCase()));
        }
    }
}

package Core.Configuration;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Getter
public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private final Config config;

    public ConfigLoader() {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        try {
            config = yaml.load(Files.readString(Paths.get("config.yaml")));
        } catch (IOException e) {
            log.error("Failed to load configuration");
            throw new RuntimeException("Could not load configuration file");
        }
    }
}

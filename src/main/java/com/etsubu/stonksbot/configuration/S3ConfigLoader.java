package com.etsubu.stonksbot.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

public class S3ConfigLoader implements IConfigLoader{
    private static final Logger log = LoggerFactory.getLogger(S3ConfigLoader.class);
    private static final int DEFAULT_REFRESH_TTL = 15 * 1000 * 60; // 15min
    private final S3Config s3Config;
    private final AmazonS3 s3;
    private long lastLoaded;
    private Instant previouslyLastModified = Instant.EPOCH;
    private Config config = new Config();
    private final Yaml yaml;

    public S3ConfigLoader(S3Config s3Config) {
        this.s3Config = s3Config;
        s3 = AmazonS3ClientBuilder.standard().withRegion(s3Config.getRegion()).build();
        lastLoaded = 0;
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()), representer);
    }
    @Override
    public Config loadConfig() {
        if(System.currentTimeMillis() - lastLoaded < Optional.ofNullable(s3Config.getRefreshTTL()).orElse(DEFAULT_REFRESH_TTL)) {
            return config;
        }
        lastLoaded = System.currentTimeMillis();
        try {
            var obj = s3.getObject(s3Config.getS3bucket(), s3Config.getFile());
            var lastModified = obj.getObjectMetadata().getLastModified().toInstant();
            if(lastModified.isAfter(this.previouslyLastModified)) {
                log.info("Loading configs from s3");
                this.previouslyLastModified = lastModified;
                var rawConfig = new String(s3.getObject(s3Config.getS3bucket(), s3Config.getFile()).getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
                config = yaml.load(rawConfig);
                config.getServers().forEach(y -> {
                    y.getReactions().forEach(Reaction::buildPattern);
                    y.getFilters().update();
                });
                return config;
            }
        } catch (IOException e) {
            log.error("Failed to load s3 configs", e);
        }
        return config;
    }
}

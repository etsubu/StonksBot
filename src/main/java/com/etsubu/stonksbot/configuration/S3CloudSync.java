package com.etsubu.stonksbot.configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Cloud sync implementation against AWS S3
 * @author etsubu
 */
public class S3CloudSync implements ConfigurationSync {
    private static final Logger log = LoggerFactory.getLogger(S3CloudSync.class);
    private final ConfigLoader configLoader;
    private final AmazonS3 s3;
    private final Gson gson;

    public S3CloudSync(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        S3Config s3Config = configLoader.getConfig().getS3();
        if(s3Config == null) {
            log.info("S3 bucket for cloud sync configured");
            throw new IllegalArgumentException("No S3 bucket configured");
        }
        gson = new Gson();
        final var builder = AmazonS3ClientBuilder.standard();
        Optional.ofNullable(s3Config.getRegion()).ifPresentOrElse(builder::withRegion, ()-> builder.withRegion(Regions.DEFAULT_REGION));
        s3 = builder.build();
    }

    @Override
    public <T> Optional<T> loadConfiguration(String key, Class<T> c) {
        String base = Optional.ofNullable(configLoader.getConfig().getS3()).map(S3Config::getS3bucket).orElse(null);
        if(base == null) {
            log.error("S3 bucket is missing");
        }
        try {
            S3Object obj = s3.getObject(base, key);
            return Optional.of(gson.fromJson(new String(obj.getObjectContent().readAllBytes(), StandardCharsets.UTF_8), c));
        } catch (Exception e) {
            log.error("Failed to load item from s3, key='{}', bucket='{}'", key, base, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean saveConfiguration(String key, Object content) {
        String base = Optional.ofNullable(configLoader.getConfig().getS3()).map(S3Config::getS3bucket).orElse(null);
        try {
            s3.putObject(base, key, gson.toJson(content));
            log.info("Saved to s3, key='{}', bucket='{}'", key, base);
            return true;
        } catch (Exception e) {
            log.error("Failed to save config with key='{}' and bucket='{}' to s3 ", key, base, e);
        }
        return false;
    }
}

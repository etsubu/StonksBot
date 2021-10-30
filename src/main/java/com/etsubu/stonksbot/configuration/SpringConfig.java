package com.etsubu.stonksbot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SpringConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringConfig.class);

    @Bean
    public ConfigLoader configLoaderBean() {
        return new ConfigLoader();
    }
    @Bean
    public TaskScheduler taskSchedulerBean() {
        var scheduler = new ThreadPoolTaskScheduler();
        int poolSize = Runtime.getRuntime().availableProcessors() * 2;
        scheduler.setPoolSize(poolSize);
        log.info("Created task scheduler with pool size '{}'", poolSize);
        return scheduler;
    }

    @Bean
    public ConfigurationSync configurationSyncBean(ConfigLoader configLoader) {
        if(configLoader.getConfig().getS3() != null) {
            log.info("Initializing AWS S3 cloud sync for configuration sync implementation.");
            try {
                return new S3CloudSync(configLoader);
            } catch (Exception e) {
                log.error("Failed to create s3 cloud sync implementation.", e);
            }
        }
        log.info("Initializing local sync for configuration sync implementation.");
        return new LocalSync();
    }
}

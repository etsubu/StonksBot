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
    public TaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        int poolSize = Runtime.getRuntime().availableProcessors() * 2;
        scheduler.setPoolSize(poolSize);
        log.info("Created task scheduler with pool size '{}'", poolSize);
        return scheduler;
    }
}

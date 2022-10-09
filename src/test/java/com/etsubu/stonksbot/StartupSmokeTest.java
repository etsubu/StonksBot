package com.etsubu.stonksbot;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Startup smoke tests to verify that spring framework initializes properly and there are not bean issues
 * @author etsubu
 */
public class StartupSmokeTest {

    @Test
    public void springInitializeSmokeTest() {
        System.setProperty("environment", "test");
        try {
            new AnnotationConfigApplicationContext(Main.class);
        } catch (Exception e) {
            fail("Spring failed to initialize", e);
        }
    }
}

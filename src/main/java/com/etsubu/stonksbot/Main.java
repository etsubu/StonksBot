package com.etsubu.stonksbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author etsubu
 * @version 26 Jul 2018
 */
@ComponentScan
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        log.info("Starting up");
        new AnnotationConfigApplicationContext(Main.class);
        log.info("Closing.");
    }
}

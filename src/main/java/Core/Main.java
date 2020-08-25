package Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.nio.charset.Charset;

/**
 * @author etsubu
 * @version 26 Jul 2018
 *
 */
@ComponentScan
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println(Charset.defaultCharset().name());
        log.info("Starting up");
        new AnnotationConfigApplicationContext(Main.class);
    }
}

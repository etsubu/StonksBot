package com.etsubu.stonksbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * System test implementation that launches bot instance with valid configurations and invokes commands on a test server
 * to validate basic functionality is working.
 *
 * @author etsubu
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(SAME_THREAD)
public class SystemTest extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SystemTest.class);
    private static JDA jda;
    private static TextChannel channel;
    private static final Map<String, Message> messages = Collections.synchronizedMap(new HashMap<>());

    /**
     * Initializes both discord bot client instances and pulls oath tokens from env variables, oath1= token used by the tester instance that issues commands
     * oath2=used by the stonksbot instance
     * @throws LoginException
     * @throws InterruptedException
     */
    @BeforeAll
    public static void init() throws LoginException, InterruptedException {
        System.setProperty("STONKSBOT_CONFIG_FILE", System.getProperty("user.dir") + "/src/integration_test/resources/config.yaml");
        System.setProperty("STONKSBOT_OATH", System.getenv("oath2"));
        Main.main(new String[]{});
        log.info("Initializing system tests {}", System.getProperty("user.dir"));
        jda = JDABuilder.createDefault(System.getenv("oath1")).build();
        jda.awaitReady();
        channel = jda.getGuilds().get(0).getDefaultChannel();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(jda.getSelfUser().getId()) || !event.getAuthor().isBot()) {
            return;
        }
        String id = Optional.ofNullable(event.getMessage().getReferencedMessage()).map(ISnowflake::getId).orElse(event.getMessageId());
        messages.put(id, event.getMessage());
        log.info("Received message: {}", event.getMessage().getContentDisplay());
    }

    private Message readMessage(String id) {
        long until = System.currentTimeMillis() + 5000;
        while(System.currentTimeMillis() < until && !messages.containsKey(id)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        return messages.remove(id);
    }

    @Test
    public void testPriceCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!p msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("MSFT") && response.getContentDisplay().contains("Price") &&
                response.getContentDisplay().contains("Change") &&
                response.getContentDisplay().contains("Open") &&
                response.getContentDisplay().contains("High") &&
                response.getContentDisplay().contains("Low") &&
                response.getContentDisplay().contains("Volume"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testStatsCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!s msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("MSFT") && response.getContentDisplay().contains("\nForward P/E: ") &&
                response.getContentDisplay().contains("\nPEG: ") &&
                response.getContentDisplay().contains("\nBeta: ") &&
                response.getContentDisplay().contains("\nTrailing EPS: ") &&
                response.getContentDisplay().contains("\nForward EPS: ") &&
                response.getContentDisplay().contains("\nForecasted EPS Growth: ") &&
                response.getContentDisplay().contains("\nProfit margin: ") &&
                response.getContentDisplay().contains("\nShort percent of float: ") &&
                response.getContentDisplay().contains("\nRecent change in short ratio: ") &&
                response.getContentDisplay().contains("\nHeld by insiders: ") &&
                response.getContentDisplay().contains("\nHeld by institutions: ") &&
                response.getContentDisplay().contains("\n52 week price change: "));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testRatiosCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!r msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("MSFT") && response.getContentDisplay().contains("\nEV/Revenue:") &&
                response.getContentDisplay().contains("\nEV/EBITDA") &&
                response.getContentDisplay().contains("\nEV/EBIT") &&
                response.getContentDisplay().contains("\nMCAP/FCF") &&
                response.getContentDisplay().contains("\nGross Profit Margin: ") &&
                response.getContentDisplay().contains("\nCurrent ratio: ") &&
                response.getContentDisplay().contains("\nROA: ") &&
                response.getContentDisplay().contains("\nP/S: ") &&
                response.getContentDisplay().contains("\nP/E: ") &&
                response.getContentDisplay().contains("\nP/B: "));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testCalendarCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!c msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("MSFT") && response.getContentDisplay().contains("\nEarnings date:") &&
                response.getContentDisplay().contains("\nForecast EPS avg") &&
                response.getContentDisplay().contains("\nForecast EPS high") &&
                response.getContentDisplay().contains("\nForecast EPS low") &&
                response.getContentDisplay().contains("\nForecast Revenue avg: ") &&
                response.getContentDisplay().contains("\nForecast Revenue high: ") &&
                response.getContentDisplay().contains("\nForecast Revenue low: ") &&
                response.getContentDisplay().contains("\nPrevious dividend date: ") &&
                response.getContentDisplay().contains("\nNext dividend date: "));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testBioCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!b msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testListCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!commands");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().split("\n").length > 5);
        assertTrue(Arrays.stream(response.getContentDisplay().split("\n")).allMatch(x -> x.startsWith("!")));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testFscore() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!f msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("\nScore:"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testZscore() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!z msft");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("\nZ-Score:"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testAboutCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!about");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().contains("Author"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testRecommendationCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!suositus outokumpu");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().startsWith("```") && response.getContentDisplay().endsWith("```"));
        assertTrue(response.getContentDisplay().contains("(Inderes)"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testHelpCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!help help");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().contains("Example"));
        log.info(response.getContentDisplay());
    }

    @Test
    public void testVotelinkCommand() throws ExecutionException, InterruptedException {
        jda.addEventListener(this);
        var action = channel.sendMessage("!lunch");
        Message message = action.submit().get();
        Message response = readMessage(message.getId());
        assertNotNull(response);
        assertTrue(response.getContentDisplay().contains("Ravintola"));
        log.info(response.getContentDisplay());
    }
}

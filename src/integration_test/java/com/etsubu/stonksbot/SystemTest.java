package com.etsubu.stonksbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System test implementation that launches bot instance with valid configurations and invokes commands on a test server
 * to validate basic functionality is working.
 *
 * @author etsubu
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SystemTest extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SystemTest.class);
    private static JDA jda;
    private Message lastMessage;
    private static TextChannel channel;

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
        if(event.getAuthor().getId().equals(jda.getSelfUser().getId())) {
            return;
        }
        lastMessage = event.getMessage();
        event.getGuild().getMembers().forEach(x -> log.info("{}:{}", x.getEffectiveName(), x.getOnlineStatus()));
        log.info("Received message: {}", event.getMessage().getContentDisplay());
    }

    private Message readMessage() {
        long until = System.currentTimeMillis() + 5000;
        Message msg = null;
        while(System.currentTimeMillis() < until && (msg = lastMessage) == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        lastMessage = null;
        return msg;
    }

    @Test
    public void testPriceCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!p msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("MSFT") && msg.getContentDisplay().contains("Price") &&
                msg.getContentDisplay().contains("Change") &&
                msg.getContentDisplay().contains("Open") &&
                msg.getContentDisplay().contains("High") &&
                msg.getContentDisplay().contains("Low") &&
                msg.getContentDisplay().contains("Volume"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testStatsCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!s msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("MSFT") && msg.getContentDisplay().contains("\nForward P/E: ") &&
                msg.getContentDisplay().contains("\nPEG: ") &&
                msg.getContentDisplay().contains("\nBeta: ") &&
                msg.getContentDisplay().contains("\nTrailing EPS: ") &&
                msg.getContentDisplay().contains("\nForward EPS: ") &&
                msg.getContentDisplay().contains("\nForecasted EPS Growth: ") &&
                msg.getContentDisplay().contains("\nProfit margin: ") &&
                msg.getContentDisplay().contains("\nShort percent of float: ") &&
                msg.getContentDisplay().contains("\nRecent change in short ratio: ") &&
                msg.getContentDisplay().contains("\nHeld by insiders: ") &&
                msg.getContentDisplay().contains("\nHeld by institutions: ") &&
                msg.getContentDisplay().contains("\n52 week price change: "));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testRatiosCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!r msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("MSFT") && msg.getContentDisplay().contains("\nEV/Revenue:") &&
                msg.getContentDisplay().contains("\nEV/EBITDA") &&
                msg.getContentDisplay().contains("\nEV/EBIT") &&
                msg.getContentDisplay().contains("\nMCAP/FCF") &&
                msg.getContentDisplay().contains("\nGross Profit Margin: ") &&
                msg.getContentDisplay().contains("\nCurrent ratio: ") &&
                msg.getContentDisplay().contains("\nROA: ") &&
                msg.getContentDisplay().contains("\nP/S: ") &&
                msg.getContentDisplay().contains("\nP/E: ") &&
                msg.getContentDisplay().contains("\nP/B: "));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testCalendarCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!c msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("MSFT") && msg.getContentDisplay().contains("\nEarnings date:") &&
                msg.getContentDisplay().contains("\nForecast EPS avg") &&
                msg.getContentDisplay().contains("\nForecast EPS high") &&
                msg.getContentDisplay().contains("\nForecast EPS low") &&
                msg.getContentDisplay().contains("\nForecast Revenue avg: ") &&
                msg.getContentDisplay().contains("\nForecast Revenue high: ") &&
                msg.getContentDisplay().contains("\nForecast Revenue low: ") &&
                msg.getContentDisplay().contains("\nPrevious dividend date: ") &&
                msg.getContentDisplay().contains("\nNext dividend date: "));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testBioCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!b msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testListCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!commands").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().split("\n").length > 5);
        assertTrue(Arrays.stream(msg.getContentDisplay().split("\n")).allMatch(x -> x.startsWith("!")));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testFscore() {
        jda.addEventListener(this);
        channel.sendMessage("!f msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("\nScore:"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testZscore() {
        jda.addEventListener(this);
        channel.sendMessage("!z msft").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("\nZ-Score:"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testAboutCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!about").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().contains("Author"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testRecommendationCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!suositus outokumpu").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().startsWith("```") && msg.getContentDisplay().endsWith("```"));
        assertTrue(msg.getContentDisplay().contains("(Inderes)"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testHelpCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!help help").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().contains("Example"));
        log.info(msg.getContentDisplay());
    }

    @Test
    public void testVotelinkCommand() {
        jda.addEventListener(this);
        channel.sendMessage("!lunch").queue();
        Message msg = readMessage();
        assertNotNull(msg);
        assertTrue(msg.getContentDisplay().contains("Ravintola"));
        log.info(msg.getContentDisplay());
    }
}

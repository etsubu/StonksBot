package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AboutCommandTest {
    private AboutCommand aboutCommand;

    @Mock
    private ConfigLoader configLoader;

    @BeforeEach
    public void init() {
        aboutCommand = new AboutCommand(configLoader);
    }

    @Test
    public void testHelp() {
        assertTrue(aboutCommand.help().length() > 0);
    }

    @Test
    public void testAbout() {
        var result = aboutCommand.exec(CommandContext.builder().message("about").build());
        assertTrue(result.isSucceeded());
        assertFalse(result.isRespondWithDM());
        assertTrue(result.getResponse().length() > 0);
    }
}

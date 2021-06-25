package com.etsubu.stonksbot.command;

import com.etsubu.stonksbot.command.utilities.CommandContext;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommandHandlerTest {
    @Mock
    private HelpCommand helpCommand;
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private Command command;
    @Mock
    private MessageReceivedEvent event;
    @Mock
    private Message message;
    @Mock
    private User user;
    @Mock
    private Member member;
    @Mock
    private Guild guild;
    @Mock
    private Role role;
    @Captor
    private ArgumentCaptor<CommandContext> context;

    private CommandHandler commandHandler;

    @BeforeEach
    public void init() {
        when(event.getGuild()).thenReturn(guild);
        when(member.getRoles()).thenReturn(List.of(role));
        when(command.getNames()).thenReturn(List.of("name1", "name2"));
        when(event.getMessage()).thenReturn(message);
        when(event.getAuthor()).thenReturn(user);
        commandHandler = new CommandHandler(List.of(command), helpCommand, configLoader);
    }

    @Test
    public void testMissingPrefix() {
        assertFalse(commandHandler.isCommand("AA"));
        assertFalse(commandHandler.isCommand(CommandHandler.COMMAND_PREFIX));
        assertTrue(commandHandler.isCommand(CommandHandler.COMMAND_PREFIX + "asd"));
    }

    @Test
    public void testGetCommand() {
        assertTrue(commandHandler.getCommand("asd").isEmpty());
        assertTrue(commandHandler.getCommand("").isEmpty());
        assertTrue(commandHandler.getCommand(null).isEmpty());
        assertTrue(commandHandler.getCommand("name1").isPresent());
        assertTrue(commandHandler.getCommand("name2").isPresent());
    }

    @Test
    public void testInvalidCommand() {
        when(message.getContentDisplay()).thenReturn("asd");
        assertFalse(commandHandler.execute(event).isSucceeded());
        when(message.getContentDisplay()).thenReturn(CommandHandler.COMMAND_PREFIX);
        assertFalse(commandHandler.execute(event).isSucceeded());
        when(message.getContentDisplay()).thenReturn(CommandHandler.COMMAND_PREFIX + "asd");
        assertFalse(commandHandler.execute(event).isSucceeded());
    }

    @Test
    public void testValidCommand() {
        when(configLoader.getConfig()).thenReturn(new Config());
        when(command.execute(any(CommandContext.class))).thenReturn(CommandResult.builder().succeeded(true).build());
        when(event.isFromGuild()).thenReturn(true);
        when(event.getMember()).thenReturn(member);
        when(message.getContentDisplay()).thenReturn(CommandHandler.COMMAND_PREFIX + "name1");
        assertTrue(commandHandler.execute(event).isSucceeded());
        verify(command, times(1)).execute(context.capture());
        assertTrue(context.getValue().getServerConfig().isEmpty());
        assertEquals("", context.getValue().getMessage());
        assertNotNull(context.getValue().getSender());
        assertEquals(1, context.getValue().getUserRoles().size());

        when(message.getContentDisplay()).thenReturn(CommandHandler.COMMAND_PREFIX + "name1 param");
        assertTrue(commandHandler.execute(event).isSucceeded());
        verify(command, times(2)).execute(context.capture());
        assertEquals("param", context.getValue().getMessage());
    }
}

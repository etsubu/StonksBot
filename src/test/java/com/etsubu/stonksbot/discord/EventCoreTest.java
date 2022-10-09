package com.etsubu.stonksbot.discord;

import com.etsubu.stonksbot.administrative.GuildRoleManager;
import com.etsubu.stonksbot.command.CommandHandler;
import com.etsubu.stonksbot.command.CommandResult;
import com.etsubu.stonksbot.configuration.Config;
import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.permission.PermissionManager;
import com.etsubu.stonksbot.scheduler.omxnordic.Model.AttachmentFile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventCoreTest {
    private static final String ID = "1234";
    @Mock
    private CommandHandler commandHandler;
    @Mock
    private ConfigLoader configLoader;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private MessageReacter reacter;
    @Mock
    private FilterHandler filterHandler;
    @Mock
    private User user;
    @Mock
    private CacheRestAction<PrivateChannel> restAction;
    @Mock
    private CacheRestAction<Void> restActionVoid;
    @Mock
    private MessageReceivedEvent event;
    @Mock
    private JDA jda;
    @Mock
    private SelfUser selfUser;
    @Mock
    private Message message;
    @Mock
    private Member member;
    @Mock
    private RichCustomEmoji emote;
    @Mock
    private MessageChannelUnion channel;
    @Mock
    private MessageCreateAction msgAction;
    @Mock
    private TextChannel guildChannel;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    @Mock
    private GuildRoleManager roleManager;


    private EventCore eventCore;

    @BeforeEach
    public void init() {
        when(configLoader.getConfig()).thenReturn(new Config());
        when(guildChannel.sendMessage(anyString())).thenReturn(msgAction);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(jda.getTextChannelById(1L)).thenReturn(guildChannel);
        when(event.getMessage()).thenReturn(message);
        when(message.reply(anyString())).thenReturn(msgAction);
        when(event.getChannel()).thenReturn(channel);
        when(channel.getName()).thenReturn("test_channel");
        when(channel.sendMessage(anyString())).thenReturn(msgAction);
        when(selfUser.getId()).thenReturn(ID);
        when(event.getAuthor()).thenReturn(user);
        when(user.openPrivateChannel()).thenReturn(restAction);
        eventCore = new EventCore(commandHandler, configLoader, permissionManager, reacter, filterHandler, roleManager);
        eventCore.registerJDA(jda);
    }

    @Test
    public void testSendPrivateMessage() {
        eventCore.sendPrivateMessage(user, "test_msg");
        verify(user, times(1)).openPrivateChannel();
        verify(restAction, times(1)).queue(any());
    }

    @Test
    public void testSendMessage() {
        when(msgAction.addFiles(anyCollection())).thenReturn(msgAction);
        assertTrue(eventCore.sendMessage(List.of(1L, 2L), "msg", List.of(new AttachmentFile(new byte[]{}, "asd"))));
        verify(msgAction, times(1)).queue();
        verify(msgAction, times(1)).addFiles(anyCollection());
    }

    @Test
    public void testReceiveMessageFromSelf() {
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn(ID);
        eventCore.onMessageReceived(event);
        verify(reacter, times(0)).react(any(MessageReceivedEvent.class));
        verify(filterHandler, times(0)).shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class));
        verify(commandHandler, times(0)).isCommand(anyString());
    }

    @Test
    public void testReactsToMessages() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("some_name");
        when(reacter.react(any(MessageReceivedEvent.class))).thenReturn(Optional.empty());
        eventCore.onMessageReceived(event);
        when(reacter.react(any(MessageReceivedEvent.class))).thenReturn(Optional.of(emote));
        when(message.addReaction(any(RichCustomEmoji.class))).thenReturn(restActionVoid);
        eventCore.onMessageReceived(event);
        verify(message, times(1)).addReaction(emote);
        verify(restActionVoid, times(1)).queue();
    }

    @Test
    public void testMessageFiltering() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), nullable(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(true);
        eventCore.onMessageReceived(event);
        when(filterHandler.shouldBeFiltered(any(Message.class), nullable(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        eventCore.onMessageReceived(event);
        verify(filterHandler, times(2)).shouldBeFiltered(any(Message.class), nullable(Member.class), any(User.class), any(GenericMessageEvent.class));
        verify(commandHandler, times(1)).isCommand(anyString());
    }

    @Test
    public void testIgnoreNonCommands() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), nullable(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(false);
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(0)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(0)).execute(event);
    }

    @Test
    public void testReplyNotAllowed() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), nullable(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(false);
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(0)).execute(event);
    }

    @Test
    public void testCommandThrowsException() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(true);
        when(commandHandler.execute(any(MessageReceivedEvent.class))).thenThrow(new NumberFormatException("asd"));
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(1)).execute(event);
        verify(message, times(1)).reply(messageCaptor.capture());
        verify(msgAction, times(1)).queue();
        assertTrue(messageCaptor.getValue().contains("unexpected exception"));
    }

    @Test
    public void testCommandReturnsBlank() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(commandHandler.execute(any(MessageReceivedEvent.class))).thenReturn(CommandResult.builder().succeeded(true).response("").build());
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(true);
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(1)).execute(event);
        verify(message, times(1)).reply(messageCaptor.capture());
        verify(msgAction, times(1)).queue();
        assertTrue(messageCaptor.getValue().contains("blank response"));
    }

    @Test
    public void testCommandFails() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(true);
        when(commandHandler.execute(any(MessageReceivedEvent.class))).thenReturn(CommandResult.builder().succeeded(false).response("asd").build());

        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(1)).execute(event);
        verify(message, times(1)).reply(messageCaptor.capture());
        verify(msgAction, times(1)).queue();
        assertEquals("asd", messageCaptor.getValue());
    }

    @Test
    public void testCommandReplyToChannel() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(commandHandler.execute(any(MessageReceivedEvent.class))).thenReturn(CommandResult.builder().succeeded(true).response("asd").build());
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(true);
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(1)).execute(event);
        verify(message, times(1)).reply(messageCaptor.capture());
        verify(msgAction, times(1)).queue();
        assertEquals("asd", messageCaptor.getValue());
    }

    @Test
    public void testCommandReplyWithDM() {
        when(message.getContentDisplay()).thenReturn("asd");
        when(user.isBot()).thenReturn(false);
        when(user.getId()).thenReturn("123");
        when(filterHandler.shouldBeFiltered(any(Message.class), any(Member.class), any(User.class), any(GenericMessageEvent.class))).thenReturn(false);
        when(commandHandler.isCommand(anyString())).thenReturn(true);
        when(permissionManager.isReplyAllowed(any(MessageReceivedEvent.class))).thenReturn(true);
        when(commandHandler.execute(any(MessageReceivedEvent.class))).thenReturn(CommandResult.builder().succeeded(true).respondWithDM(true).response("asd").build());
        eventCore.onMessageReceived(event);
        verify(permissionManager, times(1)).isReplyAllowed(any(MessageReceivedEvent.class));
        verify(commandHandler, times(1)).execute(event);
        verify(message, times(1)).reply(messageCaptor.capture());
        verify(msgAction, times(1)).queue();
        verify(user, times(1)).openPrivateChannel();
        verify(restAction, times(1)).queue(any());
        assertTrue(messageCaptor.getValue().contains("Responded"));
    }
}

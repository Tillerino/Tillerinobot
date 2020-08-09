package org.tillerino.ppaddict.chat.irc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.pircbotx.hooks.types.GenericUserEvent;
import org.pircbotx.output.OutputRaw;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.Sighted;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;
import org.tillerino.ppaddict.util.TestClock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class IrcHooksTest {
	@Mock
	GameChatEventQueue eventHandler;

	@Mock
	GameChatMetrics botInfo;

	@Mock
	Pinger pinger;

	@Mock
	IrcWriter queue;

	private TestClock clock = new TestClock();

	IrcHooks irc;

	@Mock
	CloseableBot bot;

	@Mock
	User user;

	@Mock
	OutputRaw outputRaw;

	@Mock
	Consumer<Map<String, String>> mdc;

	@Before
	public final void before() throws Exception {
		clock.set(123);
		irc = new IrcHooks(eventHandler, botInfo, pinger, false, queue, clock);
		when(bot.sendRaw()).thenReturn(outputRaw);
		doAnswer(x -> {
			System.out.println("Sending " + x.getArguments()[0]);
			return null;
		}).when(outputRaw).rawLine(anyString());
		doAnswer(x -> {
			mdc.accept(MDC.getCopyOfContextMap());
			return null;
		}).when(eventHandler).onEvent(any());
		when(user.getNick()).thenReturn("userNick");
		Configuration<PircBotX> configuration = mock(Configuration.class);
		when(configuration.getName()).thenReturn("bot_name");
		when(bot.getConfiguration()).thenReturn(configuration);
	}

	@Test
	public void testConnect() throws Exception {
		irc.onEvent(mockEvent(ConnectEvent.class));
		verify(botInfo).setRunningSince(123);
		verify(queue).setBot(bot);
	}

	@Test
	public void listOnlineUsers() throws Exception {
		// one hour after connecting
		clock.advanceBy(60 * 60 * 1000);
		irc.onEvent(mockEvent(Event.class));
		// all online users should be listed
		verify(outputRaw).rawLine("NAMES #osu");
		// a little under an hour later
		clock.advanceBy(60 * 60 * 1000 - 1);
		irc.onEvent(mockEvent(Event.class));
		// this time nothing happens
		verifyNoMoreInteractions(outputRaw);
	}

	@Test
	public void testMessage() throws Exception {
		irc.onEvent(mockEvent(MessageEvent.class));
		verify(botInfo).setLastInteraction(123);
		verify(botInfo).setLastReceivedMessage(123);
		// plain (non-private) messages are not handled
		verifyNoInteractions(eventHandler);
	}

	@Test
	public void testPrivateMessage() throws Exception {
		irc.onEvent(mockPrivateMessage("pm"));
		verify(botInfo).setLastReceivedMessage(123);
		verify(eventHandler).onEvent(new PrivateMessage(12_300, "userNick", 123, "pm"));

		// check if the event ID increments
		irc.onEvent(mockPrivateMessage("pm2"));
		verify(eventHandler).onEvent(new PrivateMessage(12_301, "userNick", 123, "pm2"));
	}

	@Test
	public void testPrivateAction() throws Exception {
		irc.onEvent(mockPrivateAction("pa"));
		verify(botInfo).setLastReceivedMessage(123);
		verify(eventHandler).onEvent(new PrivateAction(12_300, "userNick", 123, "pa"));

		// check if the event ID increments
		irc.onEvent(mockPrivateAction("pa2"));
		verify(eventHandler).onEvent(new PrivateAction(12_301, "userNick", 123, "pa2"));
	}

	@Test
	public void testJoin() throws Exception {
		irc.onEvent(mockEvent(JoinEvent.class));
		verify(eventHandler).onEvent(new Joined(12_300, "userNick", 123));
	}

	@Test
	public void testJoinBotItself() throws Exception {
		doReturn("bot_name").when(user).getNick();
		irc.onEvent(mockEvent(JoinEvent.class));
		verifyNoInteractions(eventHandler);
	}

	@Test
	public void testPublicAction() throws Exception {
		ActionEvent event = mockPrivateAction("pa");
		when(event.getChannel()).thenReturn(mock(Channel.class));
		irc.onEvent(event);
		verifyNoInteractions(eventHandler);
	}

	@Test
	public void testListingOnlineUsers() throws Exception {
		ServerResponseEvent event = mockEvent(ServerResponseEvent.class);
		when(event.getParsedResponse()).thenReturn(ImmutableList.of("stuff", "nick1 @nick2 +nick3"));
		when(event.getCode()).thenReturn(353);
		irc.onEvent(event);
		verifyNoInteractions(eventHandler);
		irc.onEvent(mockPrivateMessage("bump"));
		// event 0 was original server event
		// event 1 was bump
		verify(eventHandler).onEvent(new Sighted(12_302, "nick1", 123));
		verify(eventHandler).onEvent(new Sighted(12_303, "nick2", 123));
		verify(eventHandler).onEvent(new Sighted(12_304, "nick3", 123));
	}

	@Test
	public void testOtherServerResponses() throws Exception {
		ServerResponseEvent event = mockEvent(ServerResponseEvent.class);
		lenient().when(event.getParsedResponse()).thenReturn(ImmutableList.of("whatever"));
		irc.onEvent(event);
		verifyNoInteractions(eventHandler);
	}

	@Test
	public void eventIdIsPutIntoMdc() throws Exception {
		irc.onEvent(mockPrivateMessage(""));

		verify(mdc).accept(ImmutableMap.of("event", "12300", "user", "userNick"));

		assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
	}

	@Test
	public void unknownEventsAreForwardedToPinger() throws Exception {
		UnknownEvent event = mockEvent(UnknownEvent.class);
		irc.onEvent(event);
		verify(pinger).handleUnknownEvent(event);
	}

	<T extends Event> T mockEvent(Class<T> eventClass) {
		T event = mock(eventClass);
		when(event.getBot()).thenReturn(bot);
		when(event.getTimestamp()).thenReturn(clock.currentTimeMillis());
		if (GenericUserEvent.class.isAssignableFrom(eventClass)) {
			when(((GenericUserEvent) event).getUser()).thenReturn(user);
		}
		return event;
	}

	PrivateMessageEvent mockPrivateMessage(String message) {
		PrivateMessageEvent event = mockEvent(PrivateMessageEvent.class);
		when(event.getMessage()).thenReturn(message);
		return event;
	}

	ActionEvent mockPrivateAction(String message) {
		ActionEvent event = mockEvent(ActionEvent.class);
		when(event.getMessage()).thenReturn(message);
		return event;
	}
}

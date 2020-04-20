package org.tillerino.ppaddict.chat.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.Bouncer.SemaphorePayload;
import org.tillerino.ppaddict.util.Clock;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;

@RunWith(MockitoJUnitRunner.class)
public class MessagePreprocessorTest {
	@Mock
	Bouncer bouncer;

	@Mock
	GameChatEventQueue queue;

	@Mock
	GameChatResponseQueue responses;

	@Mock
	LiveActivityEndpoint liveActivity;

	@Mock
	Clock clock;

	@InjectMocks
	MessagePreprocessor preprocessor;

	@Test
	public void testRegularMessage() throws Exception {
		PrivateMessage event = new PrivateMessage(1, "nick", 2, "lo");

		when(bouncer.tryEnter(event.getNick(), event.getEventId())).thenReturn(true);

		preprocessor.onEvent(event);

		verify(liveActivity).propagateReceivedMessage(event.getNick(), event.getEventId());

		verify(queue).onEvent(event);
		verifyZeroInteractions(responses);
	}

	@Test
	public void testRegularAction() throws Exception {
		PrivateAction event = new PrivateAction(1, "nick", 2, "lo");

		when(bouncer.tryEnter(event.getNick(), event.getEventId())).thenReturn(true);

		preprocessor.onEvent(event);

		verify(liveActivity).propagateReceivedMessage(event.getNick(), event.getEventId());

		verify(queue).onEvent(event);
		verifyZeroInteractions(responses);
	}

	@Test
	public void testNonInteractiveMessage() throws Exception {
		Joined event = new Joined(1, "nick", 2);

		preprocessor.onEvent(event);
		verifyZeroInteractions(bouncer);
		verifyZeroInteractions(responses);
		verify(queue).onEvent(event);
	}

	@Test
	public void bouncerDenies() throws Exception {
		// bouncer returns false by default
		PrivateMessage event = new PrivateMessage(1, "nick", 2, "lo");
		when(bouncer.get("nick")).thenReturn(Optional.of(new SemaphorePayload(0, 0, 0, false)));
		preprocessor.onEvent(event);
		verify(bouncer).tryEnter("nick", 1);
		verify(bouncer).get("nick");
		verifyNoMoreInteractions(bouncer);
		verify(responses).onResponse(GameChatResponse.none(), event);
		verifyZeroInteractions(queue);
	}

	@Test
	public void bouncerDeniesAndThenLateWarning() throws Exception {
		// bouncer returns false by default
		// fewer than three attempts within five seconds, so we get the chill response
		when(clock.currentTimeMillis()).thenReturn(5001L);
		PrivateMessage event = new PrivateMessage(1, "nick", 5001, "lo");
		when(bouncer.get("nick")).thenReturn(Optional.of(new SemaphorePayload(0, 0, 0, false)));
		when(bouncer.updateIfPresent(eq("nick"), eq(0L), any())).thenReturn(true);
		preprocessor.onEvent(event);
		verify(bouncer).tryEnter("nick", 1);
		verify(bouncer).get("nick");
		verify(bouncer).updateIfPresent(eq("nick"), eq(0L), any());
		verifyNoMoreInteractions(bouncer);
		verify(responses).onResponse(new CommandHandler.Message("Just a second..."), event);
		verify(queue).size();
		verifyNoMoreInteractions(queue);
	}

	@Test
	public void bouncerDeniesAndThenSpamWarning() throws Exception {
		// bouncer returns false by default
		// three attempts within less than five seconds, so we get clicky meme
		when(clock.currentTimeMillis()).thenReturn(2001L);
		PrivateMessage event = new PrivateMessage(1, "nick", 2001, "lo");
		when(bouncer.get("nick")).thenReturn(Optional.of(new SemaphorePayload(0, 0, 3, false)));
		when(bouncer.updateIfPresent(eq("nick"), eq(0L), any())).thenReturn(true);
		preprocessor.onEvent(event);
		verify(bouncer).tryEnter("nick", 1);
		verify(bouncer).get("nick");
		verify(bouncer).updateIfPresent(eq("nick"), eq(0L), any());
		verifyNoMoreInteractions(bouncer);
		verify(responses).onResponse(new CommandHandler.Message("[http://i.imgur.com/Ykfua8r.png ...]"), event);
		verifyZeroInteractions(queue);
	}
}

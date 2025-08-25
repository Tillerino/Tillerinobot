package org.tillerino.ppaddict.chat.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.tillerino.ppaddict.util.Result.err;
import static org.tillerino.ppaddict.util.Result.ok;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.*;
import org.tillerino.ppaddict.chat.GameChatResponse.Action;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;
import org.tillerino.ppaddict.util.PhaseTimer;

public class ResponsePostprocessorTest {
	@Spy
	private LocalGameChatMetrics botInfo = new LocalGameChatMetrics();

	@Mock
	private Bouncer bouncer;

	@Mock
	private LiveActivity liveActivity;

	@Mock
	private GameChatWriter writer;

	@Mock
	private Clock clock;

	@InjectMocks
	private ResponsePostprocessor responsePostprocessor;

	PrivateMessage event = new PrivateMessage(1, "nick", 2, "yo");

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(writer.action(any(), any())).thenReturn(ok(new GameChatWriter.Response(null)));
		when(writer.message(any(), any())).thenReturn(ok(new GameChatWriter.Response(null)));
		event.getMeta().setTimer(new PhaseTimer());
	}

	@Test
	public void testAction() throws Exception {
		responsePostprocessor.onResponse( new Action("xyz"), event);
		verify(writer).action("xyz", "nick");
		verify(liveActivity).propagateSentMessage("nick", 1, null);
		verify(bouncer).exit("nick", 1);
	}

	@Test
	public void testMessage() throws Exception {
		responsePostprocessor.onResponse(new Message("xyz"), event);
		verify(writer).message("xyz", "nick");
		verify(liveActivity).propagateSentMessage("nick", 1, null);
		verify(bouncer).exit("nick", 1);
		
	}

	@Test
	public void testSuccess() throws Exception {
		responsePostprocessor.onResponse(new Success("xyz"), event);
		verify(writer).message("xyz", "nick");
		verify(liveActivity).propagateSentMessage("nick", 1, null);
		verify(bouncer).exit("nick", 1);
	}

	@Test
	public void testList() throws Exception {
		responsePostprocessor.onResponse(new Message("xyz").then(new Action("abc")), event);
		verify(writer).message("xyz", "nick");
		verify(writer).action("abc", "nick");
		verify(liveActivity, times(2)).propagateSentMessage("nick", 1, null);
		verify(bouncer, times(1)).exit("nick", 1);
	}

	@Test
	public void testNoResponse() throws Exception {
		responsePostprocessor.onResponse(GameChatResponse.none(), event);
		verifyNoInteractions(writer);
		verifyNoInteractions(liveActivity);
		verify(bouncer, times(1)).exit("nick", 1);
	}

	@Test
	public void testRecommendation() throws Exception {
		when(clock.currentTimeMillis()).thenReturn(159L);
		try (MdcAttributes mdc = MdcUtils.with("handler", "r")) {
			responsePostprocessor.onResponse(new Success("xyz"), event);
			verify(botInfo).setLastRecommendation(159);
		}
	}

	@Test
	public void testNoBouncerForNonInteractiveEvents() throws Exception {
		Sighted event = new Sighted(1, "nick", 2);
		event.getMeta().setTimer(new PhaseTimer());
		responsePostprocessor.onResponse(new Success("hai"), event);
		verify(writer).message("hai", "nick");
		verifyNoInteractions(bouncer);
	}

	@Test
	public void testMdcThrowup() throws Exception {
		// this is a bit ugly, but since we know that this method is called,
		// we can hook into it and validate the MDC :/
		doAnswer(x -> {
			assertThat(MDC.get("state")).isEqualTo("sent");
			assertThat(MDC.get("success")).isEqualTo("true");
			assertThat(MDC.get("duration")).isEqualTo("121");
			assertThat(MDC.get("osuApiRateBlockedTime")).isEqualTo("32");
			return null;
		}).when(botInfo).setLastSentMessage(123L);
		event.getMeta().setRateLimiterBlockedTime(32);
		when(clock.currentTimeMillis()).thenReturn(123L);
		responsePostprocessor.onResponse(new Success("yeah"), event);
		verify(botInfo).setLastSentMessage(123L);
	}

	@Test
	public void testNoMdcThrowupForPlainMessage() throws Exception {
		// this is a bit ugly, but since we know that this method is called,
		// we can hook into it and validate the MDC :/
		doAnswer(x -> {
			assertThat(MDC.get("state")).isEqualTo("sent");
			assertThat(MDC.get("success")).isNull();
			assertThat(MDC.get("duration")).isNull();
			assertThat(MDC.get("osuApiRateBlockedTime")).isNull();
			return null;
		}).when(botInfo).setLastSentMessage(123L);
		event.getMeta().setRateLimiterBlockedTime(32);
		when(clock.currentTimeMillis()).thenReturn(123L);
		responsePostprocessor.onResponse(new Message("yeah"), event);
		verify(botInfo).setLastSentMessage(123L);
	}

	@Test
	public void writingIsRetried() throws Exception {
		int[] count = { 0 };
		doAnswer(x -> {
			if (count[0]++ > 0) {
				return ok(new GameChatWriter.Response(null));
			}
			return err(new GameChatWriter.Error.Retry(0));
		}).when(writer).message("abc", "nick");
		responsePostprocessor.onResponse(new Message("abc"), event);
		verify(writer, times(2)).message("abc", "nick");
	}

	@Test
	public void retryingStops() throws Exception {
		doReturn(err(new GameChatWriter.Error.Retry(0))).when(writer).message("abc", "nick");
		responsePostprocessor.onResponse(new Message("abc"), event);
		verify(writer, times(10)).message("abc", "nick");
	}
}

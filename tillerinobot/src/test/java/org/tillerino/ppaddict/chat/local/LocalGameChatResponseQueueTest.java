package org.tillerino.ppaddict.chat.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.ResponsePostprocessor;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;
import tillerino.tillerinobot.testutil.ExecutorServiceRule;

@RunWith(MockitoJUnitRunner.class)
public class LocalGameChatResponseQueueTest {
	@Mock
	private BotInfo botInfo;

	@Mock
	private ResponsePostprocessor downstream;

	@InjectMocks
	private LocalGameChatResponseQueue queue;

	@Rule
	public final ExecutorServiceRule exec = ExecutorServiceRule.singleThread("response-queue");

	private Future<?> queueFuture;

	private final PrivateMessage event = new PrivateMessage(1, "nick", 123, "lo");

	@After
	public void tearDown() throws Exception {
		queueFuture.cancel(true);
	}

	@Test
	public void testRegularHandoff() throws Exception {
		queueFuture = exec.submit(queue);

		queue.onResponse(Response.none(), event);
		verify(downstream, timeout(1000)).onResponse(Response.none(), event);
	}

	@Test
	public void testQueueSize() throws Exception {
		queue.onResponse(Response.none(), event);
		verify(botInfo, only()).setResponseQueueSize(1);
		reset(botInfo);
		assertThat(queue.size()).as("Declared queue size").isEqualTo(1);

		queueFuture = exec.submit(queue);
		verify(downstream, timeout(1000)).onResponse(Response.none(), event);
		verify(botInfo, only()).setResponseQueueSize(0);
		assertThat(queue.size()).as("Declared queue size").isEqualTo(0);
	}

	@Test
	public void testInterruptStops() throws Exception {
		queueFuture = exec.submit(queue);

		doThrow(InterruptedException.class).when(downstream).onResponse(any(), any());
		queue.onResponse(Response.none(), event);
		await().until(() -> queueFuture.isDone());
	}

	@Test
	public void exceptionsDontStopTheQueue() throws Exception {
		queueFuture = exec.submit(queue);

		doThrow(Exception.class).when(downstream).onResponse(new Message("throw"), event);
		queue.onResponse(new Message("throw"), event);
		queue.onResponse(Response.none(), event);
		verify(downstream, timeout(1000)).onResponse(new Message("throw"), event);
		verify(downstream, timeout(1000)).onResponse(Response.none(), event);
	}

	@Test
	public void testMdc() throws Exception {
		doAnswer(x -> {
			assertThat(MDC.get("someKey")).isEqualTo("someVal");
			return null;
		}).when(downstream).onResponse(any(), any());

		queueFuture = exec.submit(queue);
		try (MdcAttributes mdc = MdcUtils.with("someKey", "someVal")) {
			queue.onResponse(Response.none(), event);
		}

		verify(downstream, timeout(1000)).onResponse(any(), any());
	}
}

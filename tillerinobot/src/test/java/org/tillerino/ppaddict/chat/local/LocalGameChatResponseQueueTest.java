package org.tillerino.ppaddict.chat.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import tillerino.tillerinobot.testutil.ExecutorServiceRule;

@RunWith(MockitoJUnitRunner.class)
public class LocalGameChatResponseQueueTest {
	@Mock
	private GameChatMetrics botInfo;

	@Mock
	private GameChatResponseConsumer downstream;

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

		queue.onResponse(GameChatResponse.none(), event);
		verify(downstream, timeout(1000)).onResponse(GameChatResponse.none(), event);
	}

	@Test
	public void testQueueSize() throws Exception {
		queue.onResponse(GameChatResponse.none(), event);
		verify(botInfo, only()).setResponseQueueSize(1);
		reset(botInfo);
		assertThat(queue.size()).as("Declared queue size").isEqualTo(1);

		queueFuture = exec.submit(queue);
		verify(downstream, timeout(1000)).onResponse(GameChatResponse.none(), event);
		verify(botInfo, only()).setResponseQueueSize(0);
		assertThat(queue.size()).as("Declared queue size").isEqualTo(0);
	}

	@Test
	public void testInterruptStops() throws Exception {
		queueFuture = exec.submit(queue);

		doThrow(InterruptedException.class).when(downstream).onResponse(any(), any());
		queue.onResponse(GameChatResponse.none(), event);
		await().until(() -> queueFuture.isDone());
	}

	@Test
	public void exceptionsDontStopTheQueue() throws Exception {
		queueFuture = exec.submit(queue);

		doThrow(RuntimeException.class).when(downstream).onResponse(new Message("throw"), event);
		queue.onResponse(new Message("throw"), event);
		queue.onResponse(GameChatResponse.none(), event);
		verify(downstream, timeout(1000)).onResponse(new Message("throw"), event);
		verify(downstream, timeout(1000)).onResponse(GameChatResponse.none(), event);
	}

	@Test
	public void testMdc() throws Exception {
		doAnswer(x -> {
			assertThat(MDC.get("someKey")).isEqualTo("someVal");
			return null;
		}).when(downstream).onResponse(any(), any());

		queueFuture = exec.submit(queue);
		try (MdcAttributes mdc = MdcUtils.with("someKey", "someVal")) {
			queue.onResponse(GameChatResponse.none(), event);
		}

		verify(downstream, timeout(1000)).onResponse(any(), any());
	}
}

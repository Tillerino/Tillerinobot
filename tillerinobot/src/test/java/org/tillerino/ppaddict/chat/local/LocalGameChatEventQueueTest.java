package org.tillerino.ppaddict.chat.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;
import tillerino.tillerinobot.testutil.ExecutorServiceRule;

@RunWith(MockitoJUnitRunner.class)
public class LocalGameChatEventQueueTest {
	@Mock
	private BotInfo botInfo;

	@Mock
	private IRCBot coreHandler;

	@Rule
	public final ExecutorServiceRule exec = new ExecutorServiceRule(() -> Executors.newFixedThreadPool(2));

	private LocalGameChatEventQueue queue;

	private Future<?> queueRunner;

	@Before
	public void before() {
		queue = new LocalGameChatEventQueue(coreHandler, exec, botInfo);
		queueRunner = exec.submit(queue);
	}

	@After
	public void after() {
		queueRunner.cancel(true);
	}

	@Test
	public void testSimpleQueue() throws Exception {
		PrivateMessage event = new PrivateMessage(1, "sender", 125, "hello");
		queue.onEvent(event);
		verify(coreHandler, timeout(1000)).onEvent(event);
	}

	@Test
	public void testMdc() throws Exception {
		try (MdcAttributes mdc = MdcUtils.with("someKey", "someVal")) {
			doAnswer(x -> {
				assertThat(MDC.get("someKey")).isEqualTo("someVal");
				return null;
			}).when(coreHandler).onEvent(any());
			queue.onEvent(new PrivateMessage(1, "sender", 125, "hello"));
		}
		verify(coreHandler, timeout(1000)).onEvent(any());
	}

	@Test
	public void testQueueSizes() throws Exception {
		ThreadPoolExecutor exec = mock(ThreadPoolExecutor.class);
		BlockingQueue blockingQueue = new LinkedBlockingQueue<>();
		when(exec.getQueue()).thenReturn(blockingQueue);
		when(exec.submit(any(Runnable.class))).thenAnswer(x -> {
			blockingQueue.put(x.getArguments()[0]);
			return null;
		});
		LocalGameChatEventQueue queue = new LocalGameChatEventQueue(coreHandler, exec, botInfo);
		queue.onEvent(new PrivateMessage(1, "sender", 125, "hello"));
		verify(botInfo, only()).setEventQueueSize(1);
		verify(exec, never()).submit(any(Runnable.class));
		reset(botInfo);

		queue.loop();
		verify(exec).submit(any(Runnable.class));
		verify(botInfo, only()).setEventQueueSize(1);
	}
}

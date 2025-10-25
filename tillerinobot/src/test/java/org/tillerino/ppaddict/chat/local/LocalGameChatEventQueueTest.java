package org.tillerino.ppaddict.chat.local;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.IRCBot;

public class LocalGameChatEventQueueTest {
    private final LocalGameChatMetrics botInfo = mock(LocalGameChatMetrics.class);

    private final IRCBot coreHandler = mock(IRCBot.class);

    @RegisterExtension
    public final ExecutorServiceRule exec = new ExecutorServiceRule(() -> Executors.newFixedThreadPool(2));

    private LocalGameChatEventQueue queue;

    private Future<?> queueRunner;

    PrivateMessage event0 = new PrivateMessage(0, "sender", 125, "hello");
    PrivateMessage event1 = new PrivateMessage(1, "sender", 125, "hello");

    @BeforeEach
    public void before() {
        queue = new LocalGameChatEventQueue(
                new MessageHandlerScheduler(coreHandler, (ThreadPoolExecutor) exec.getExec()), botInfo);
        queueRunner = exec.submit(queue);
        event0.getMeta().setTimer(new PhaseTimer());
        event1.getMeta().setTimer(new PhaseTimer());
    }

    @AfterEach
    public void after() {
        queueRunner.cancel(true);
    }

    @Test
    public void testSimpleQueue() throws Exception {
        queue.onEvent(event1);
        verify(coreHandler, timeout(1000)).onEvent(event1);
    }

    @Test
    public void testMdc() throws Exception {
        try (MdcAttributes mdc = MdcUtils.with("someKey", "someVal")) {
            doAnswer(x -> {
                        assertThat(MDC.get("someKey")).isEqualTo("someVal");
                        return null;
                    })
                    .when(coreHandler)
                    .onEvent(any());
            queue.onEvent(event1);
        }
        verify(coreHandler, timeout(1000)).onEvent(any());
    }

    @Test
    public void testQueueSizes() throws Exception {
        ThreadPoolExecutor exec = mock(ThreadPoolExecutor.class);
        LocalGameChatEventQueue queue =
                new LocalGameChatEventQueue(new MessageHandlerScheduler(coreHandler, exec), botInfo);
        queue.onEvent(event1);
        verify(botInfo, only()).setEventQueueSize(1);
        verify(exec, never()).submit(any(Runnable.class));
        reset(botInfo);

        queue.loop();
        verify(exec).submit(any(Runnable.class));
        verify(botInfo, only()).setEventQueueSize(0);
    }

    @Test
    public void queueSizeEventuallyReachesZero() throws Exception {
        List<CountDownLatch> arrived =
                IntStream.range(0, 2).mapToObj(x -> new CountDownLatch(1)).collect(toList());
        List<CountDownLatch> leavePlease =
                IntStream.range(0, 2).mapToObj(x -> new CountDownLatch(1)).collect(toList());

        doAnswer(x -> {
                    arrived.get((int) ((GameChatEvent) x.getArgument(0)).getEventId())
                            .countDown();
                    leavePlease
                            .get((int) ((GameChatEvent) x.getArgument(0)).getEventId())
                            .await();
                    return null;
                })
                .when(coreHandler)
                .onEvent(any());

        doAnswer(x -> {
                    System.out.printf("setting to %s%n", (Long) x.getArgument(0));
                    return null;
                })
                .when(botInfo)
                .setEventQueueSize(anyLong());

        // we unwrap the executor here since we need to access the queue
        LocalGameChatEventQueue queue = new LocalGameChatEventQueue(
                new MessageHandlerScheduler(coreHandler, (ThreadPoolExecutor) exec.getExec()), botInfo);

        // since one thread in the pool is occupied, we can block the executor pool with a single task
        queue.onEvent(event0);
        queue.onEvent(event1);
        verify(botInfo).setEventQueueSize(2L);

        // schedule
        queue.loop();
        queue.loop();

        arrived.get(0).await(1, TimeUnit.SECONDS);
        // the first is now scheduled and running, let's release it
        leavePlease.get(0).countDown();

        // the second will now get scheduled
        arrived.get(1).await(1, TimeUnit.SECONDS);

        try {
            // the queue now should drop to zero
            verify(botInfo, timeout(1000)).setEventQueueSize(0L);
        } finally {
            // clean up
            leavePlease.get(1).countDown();
        }
    }
}

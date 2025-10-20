package org.tillerino.ppaddict.chat.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.tillerino.ppaddict.chat.*;
import org.tillerino.ppaddict.util.LoopingRunnable;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

/**
 * Implements {@link GameChatResponseQueue} with a simple local, in-memory version. If {@link #run()} is executed, the
 * queue feeds the queued responses synchronously downstream.
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LocalGameChatResponseQueue extends LoopingRunnable implements GameChatResponseQueue {
    private final @Named("responsePostprocessor") GameChatResponseConsumer downstream;

    private final BlockingQueue<Pair<GameChatResponse, GameChatEvent>> queue = new LinkedBlockingQueue<>();

    private final LocalGameChatMetrics botInfo;

    @Override
    public void onResponse(GameChatResponse response, GameChatEvent event) throws InterruptedException {
        event.getMeta().setMdc(MdcUtils.getSnapshot());
        queue.put(Pair.of(response, event));
        botInfo.setResponseQueueSize(queue.size());
    }

    @Override
    protected void loop() throws InterruptedException {
        Pair<GameChatResponse, GameChatEvent> response = queue.take();
        botInfo.setResponseQueueSize(queue.size());
        try (MdcAttributes mdc = response.getRight().getMeta().getMdc().apply()) {
            downstream.onResponse(response.getLeft(), response.getRight());
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            log.error("Exception while handling response", e);
        }
    }

    @Override
    public int size() {
        return queue.size();
    }
}

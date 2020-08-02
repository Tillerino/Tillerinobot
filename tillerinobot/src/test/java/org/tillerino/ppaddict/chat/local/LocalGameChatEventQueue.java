package org.tillerino.ppaddict.chat.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler;
import org.tillerino.ppaddict.util.LoopingRunnable;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LocalGameChatEventQueue extends LoopingRunnable implements GameChatEventQueue {
	private final BlockingQueue<GameChatEvent> queue = new LinkedBlockingQueue<>();

	private final MessageHandlerScheduler scheduler;

	private final LocalGameChatMetrics botInfo;

	@Override
	public void onEvent(GameChatEvent event) throws InterruptedException {
		event.getMeta().setMdc(MdcUtils.getSnapshot());
		queue.put(event);
		botInfo.setEventQueueSize(size());
	}

	@Override
	protected void loop() throws InterruptedException {
		scheduler.onEvent(queue.take());
		botInfo.setEventQueueSize(size());
	}

	@Override
	public int size() {
		return queue.size();
	}
}

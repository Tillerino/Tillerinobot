package org.tillerino.ppaddict.chat.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.util.LoopingRunnable;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.IRCBot;

@Singleton
@Slf4j
public class LocalGameChatEventQueue extends LoopingRunnable implements GameChatEventQueue {
	private final BlockingQueue<GameChatEvent> queue = new LinkedBlockingQueue<>();

	private final IRCBot coreHandler;

	private final ExecutorService coreExecutorService;

	private final GameChatMetrics botInfo;

	@Inject
	public LocalGameChatEventQueue(IRCBot coreHandler, @Named("core") ExecutorService coreExecutorService, GameChatMetrics botInfo) {
		this.coreHandler = coreHandler;
		this.coreExecutorService = coreExecutorService;
		this.botInfo = botInfo;
	}

	@Override
	public void onEvent(GameChatEvent event) throws InterruptedException {
		event.getMeta().setMdc(MdcUtils.getSnapshot());
		queue.put(event);
		botInfo.setEventQueueSize(size());
	}

	@Override
	protected void loop() throws InterruptedException {
		GameChatEvent event = queue.take();
		coreExecutorService.submit(() -> {
			botInfo.setEventQueueSize(size());
			try (MdcAttributes mdc = event.getMeta().getMdc().apply()) {
				coreHandler.onEvent(event);
			} catch (Throwable e) {
				log.error("Uncaught exception in core event handler", e);
			}
		});
		botInfo.setEventQueueSize(size());
	}

	@Override
	public int size() {
		if (coreExecutorService instanceof ThreadPoolExecutor) {
			return queue.size() + ((ThreadPoolExecutor) coreExecutorService).getQueue().size();
		}
		return queue.size();
	}
}

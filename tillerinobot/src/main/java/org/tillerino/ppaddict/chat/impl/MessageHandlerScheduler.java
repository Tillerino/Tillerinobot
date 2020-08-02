package org.tillerino.ppaddict.chat.impl;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.IRCBot;

/**
 * We want to handle events in parallel, but we don't want to drain the queue
 * and put everything into the queue of the thread pool.
 * See comments below for more details.
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageHandlerScheduler implements GameChatEventConsumer {
	private final IRCBot messageHandler;

	private final @Named("core") ThreadPoolExecutor coreExecutorService;

	@Override
	public void onEvent(GameChatEvent event) throws InterruptedException {
		Runnable task = () -> {
			try (MdcAttributes attr = event.getMeta().getMdc().apply()) {
				messageHandler.onEvent(event);
			} catch (Throwable e) {
				log.error("Uncaught exception in core event handler", e);
			}
		};
		for (;;) {
			try {
				coreExecutorService.submit(task);
			} catch (RejectedExecutionException e) {
				// the SynchronousQueue will lead to a rejected exception if no free worker is
				// available
				if (coreExecutorService.isShutdown()) {
					throw new InterruptedException("Shutting down");
				}
				// in this case, we try to put the task directly into the queue
				boolean taken = coreExecutorService.getQueue().offer(task, 10, TimeUnit.MILLISECONDS);
				if (!taken) {
					// if it is not picked up by a worker, we try the normal route again, since we
					// need to maintain the pool
					continue;
				}
			}
			break;
		}
	}

	public static class MessageHandlerSchedulerModule extends AbstractModule {
		@Override
		protected void configure() {
		}

		@Provides
		@Singleton
		@Named("core")
		ThreadPoolExecutor coreExecutorService(@Named("coreSize") int coreSize) {
			final ThreadGroup group = new ThreadGroup("CoreHandlerThreads");
			return new ThreadPoolExecutor(coreSize, coreSize,
					0L, TimeUnit.MILLISECONDS,
					// synchronous queue has 0 size and will block submitter until a thread is ready
					// to take the task
					new SynchronousQueue<Runnable>(),
					(ThreadFactory) r -> new Thread(group, r, "CoreHandler"));
		}
	}
}

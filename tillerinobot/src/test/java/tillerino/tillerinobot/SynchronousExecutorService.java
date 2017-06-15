package tillerino.tillerinobot;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

/**
 * This is an {@link ExecutorService} implementation which immediately executes all tasks on the calling thread.
 */
public class SynchronousExecutorService extends AbstractExecutorService {
	@Getter
	private boolean shutdown = false;

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		shutdown();
		return Collections.emptyList();
	}

	@Override
	public boolean isTerminated() {
		return isShutdown();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return isShutdown();
	}

	@Override
	public void execute(Runnable command) {
		command.run();
	}

}

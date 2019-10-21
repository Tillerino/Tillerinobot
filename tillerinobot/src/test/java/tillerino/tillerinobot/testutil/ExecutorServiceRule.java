package tillerino.tillerinobot.testutil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.junit.rules.ExternalResource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Wraps {@link ExecutorService} in an {@link ExternalResource}.
 */
@RequiredArgsConstructor
public class ExecutorServiceRule extends ExternalResource implements ExecutorService {
	@Getter
	@Delegate(types = ExecutorService.class)
	private ExecutorService exec;

	private final Supplier<ExecutorService> supplier;

	private boolean interruptOnShutdown = false;

	@Override
	protected void before() throws Throwable {
		exec = supplier.get();
	}

	@Override
	protected void after() {
		if (interruptOnShutdown) {
			exec.shutdownNow();
		} else {
			exec.shutdown();
		}
		Awaitility.await("Executor service shut down").until(exec::isTerminated);
	}

	public ExecutorServiceRule interruptOnShutdown() {
		interruptOnShutdown = true;
		return this;
	}

	public static ExecutorServiceRule singleThread(String name) {
		return new ExecutorServiceRule(() -> Executors.newSingleThreadExecutor(r -> new Thread(r, name)));
	}

	public static ExecutorServiceRule fixedThreadPool(String name, int nThreads) {
		return new ExecutorServiceRule(() -> Executors.newFixedThreadPool(nThreads, r -> new Thread(r, name)));
	}

	public static ExecutorServiceRule cachedThreadPool(String name) {
		return new ExecutorServiceRule(() -> Executors.newCachedThreadPool(r -> new Thread(r, name)));
	}
}

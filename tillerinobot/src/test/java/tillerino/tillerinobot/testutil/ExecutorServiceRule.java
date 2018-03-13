package tillerino.tillerinobot.testutil;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.junit.rules.ExternalResource;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Wraps {@link ExecutorService} in an {@link ExternalResource}.
 */
@RequiredArgsConstructor
public class ExecutorServiceRule extends ExternalResource implements ExecutorService {
	@Delegate(types = ExecutorService.class)
	private ExecutorService exec;

	private final Supplier<ExecutorService> supplier;

	@Override
	protected void before() throws Throwable {
		super.before();
		exec = supplier.get();
	}

	@Override
	protected void after() {
		exec.shutdown();
		Awaitility.await("Executor service shut down").until(exec::isTerminated);
		super.after();
	}
}

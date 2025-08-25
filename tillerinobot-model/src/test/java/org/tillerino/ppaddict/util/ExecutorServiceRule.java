package org.tillerino.ppaddict.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Wraps {@link ExecutorService} in a JUnit 5 extension.
 */
@RequiredArgsConstructor
public class ExecutorServiceRule implements ExecutorService, BeforeEachCallback, AfterEachCallback {
	@Getter
	@Delegate(types = ExecutorService.class)
	private ExecutorService exec;

	private final Supplier<ExecutorService> supplier;

	private boolean interruptOnShutdown = false;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		exec = supplier.get();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
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

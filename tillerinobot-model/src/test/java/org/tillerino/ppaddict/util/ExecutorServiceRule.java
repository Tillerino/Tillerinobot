package org.tillerino.ppaddict.util;

import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Wraps {@link ExecutorService} in a JUnit 5 extension. */
@RequiredArgsConstructor
public class ExecutorServiceRule<T extends ExecutorService>
        implements ExecutorService, BeforeEachCallback, AfterEachCallback {
    @Getter
    @Delegate(types = ExecutorService.class)
    private T exec;

    private final Supplier<T> supplier;

    private boolean interruptOnShutdown = false;

    @Override
    public void beforeEach(ExtensionContext context) {
        exec = supplier.get();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (interruptOnShutdown) {
            exec.shutdownNow();
        } else {
            exec.shutdown();
        }
        Awaitility.await("Executor service shut down").until(exec::isTerminated);
    }

    public ExecutorServiceRule<T> interruptOnShutdown() {
        interruptOnShutdown = true;
        return this;
    }

    public static ExecutorServiceRule<ExecutorService> singleThread(String name) {
        return new ExecutorServiceRule<>(() -> Executors.newSingleThreadExecutor(r -> new Thread(r, name)));
    }

    public static ExecutorServiceRule<ExecutorService> fixedThreadPool(String name, int nThreads) {
        return new ExecutorServiceRule<>(() -> Executors.newFixedThreadPool(nThreads, r -> new Thread(r, name)));
    }

    public static ExecutorServiceRule<ExecutorService> cachedThreadPool(String name) {
        return new ExecutorServiceRule<>(() -> Executors.newCachedThreadPool(r -> new Thread(r, name)));
    }

    public static ExecutorServiceRule<ExecutorService> synchronous() {
        return new ExecutorServiceRule<>(() -> new AbstractExecutorService() {
            private final ExecutorService async =
                    new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

            @Getter
            private boolean shutdown = false;

            @Override
            public void shutdown() {
                async.shutdown();
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
            public boolean awaitTermination(long timeout, TimeUnit unit) {
                return isShutdown();
            }

            @Override
            public void execute(Runnable command) {
                Future<?> future = async.submit(command);
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new UncheckedExecutionException(e.getCause());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

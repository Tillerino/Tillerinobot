package tillerino.tillerinobot.testutil;

import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This is an {@link ExecutorService} implementation which executes all tasks on a separate thread, but waits for
 * completion of the task.
 */
public class SynchronousExecutorServiceRule implements ExecutorService, BeforeEachCallback, AfterEachCallback {
    @Delegate(types = ExecutorService.class)
    private ExecutorService exec;

    private static class Impl extends AbstractExecutorService {
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
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        exec = new Impl();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        exec.shutdown();
    }
}

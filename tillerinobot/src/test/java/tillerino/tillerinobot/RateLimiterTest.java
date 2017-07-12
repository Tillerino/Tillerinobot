package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.Test;

public class RateLimiterTest {
	private static ExecutorService exec = Executors.newCachedThreadPool();

	@AfterClass
	public static void stopExec() {
		exec.shutdownNow();
	}

	@Test
	public void testOnlyHigh() throws Exception {
		RateLimiter limiter = new RateLimiter();
		AtomicInteger high = new AtomicInteger();
		AtomicInteger low = new AtomicInteger();

		exec.submit(() -> {
			limiter.setThreadPriority(RateLimiter.REQUEST);
			for (;;) {
				limiter.limitRate();
				high.incrementAndGet();
			}
		});
		exec.submit(() -> {
			limiter.setThreadPriority(RateLimiter.MAINTENANCE);
			for (;;) {
				limiter.limitRate();
				low.incrementAndGet();
			}
		});
		IntStream.range(0, 100).forEach(x -> limiter.addPermit());
		while (high.get() + low.get() < 100) {
			Thread.sleep(10);
		}
		assertEquals(100, high.get());
	}

	@Test
	public void testSplit() throws Exception {
		RateLimiter limiter = new RateLimiter();
		AtomicInteger high = new AtomicInteger();
		AtomicInteger low = new AtomicInteger();

		CyclicBarrier barrier = new CyclicBarrier(2);

		exec.submit(() -> {
			limiter.setThreadPriority(RateLimiter.REQUEST);
			for (;;) {
				barrier.await();
				limiter.limitRate();
				high.incrementAndGet();
			}
		});
		exec.submit(() -> {
			limiter.setThreadPriority(RateLimiter.EVENT);
			for (;;) {
				limiter.limitRate();
				low.incrementAndGet();
			}
		});
		/*
		 * We add 200 permits. In this time, the high thread will request 100
		 * permits. This is ensured by the cyclic barrier.
		 */
		IntStream.range(0, 100).forEach(x -> {
			try {
				barrier.await();
				limiter.addPermit();
				limiter.addPermit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		/*
		 * Since the high thread only takes 100 permits and the high permit
		 * queue as a capacity of 50, the lower thread gets 50 permits.
		 */
		while (high.get() + low.get() < 150) {
			Thread.sleep(10);
		}
		assertEquals(100, high.get());
	} 
}

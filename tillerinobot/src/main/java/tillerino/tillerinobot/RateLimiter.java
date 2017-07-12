package tillerino.tillerinobot;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiter for the osu! API with multiple urgency levels. It keeps one
 * (bounded) queue per priority, adding 1200 permits per second to the queues,
 * highest to lowest priority. It drains the queues in reverse order, which will
 * always keep a buffer for high priority threads. The sum of the capacities of
 * the permit queues is 200, which roughly translates to "1200 requests per
 * minute, with burst capability of up to 200 beyond that", which is the
 * documentation of the rate limit of the API.
 */
@Singleton
@Slf4j
public class RateLimiter {
	/**
	 * We're answering a direct request. This has highest priority.
	 */
	public static final int REQUEST = 0;
	/**
	 * We're responding to a event, but it's probably not time-critical.
	 */
	public static final int EVENT = 1;
	/**
	 * Background maintenance work. Can always wait.
	 */
	public static final int MAINTENANCE = 2;

	/**
	 * The actual value of a permit is irrelevant.
	 */
	private static final Object PERMIT = new Object();

	private final ThreadLocal<Integer> threadPriority = ThreadLocal.withInitial(() -> MAINTENANCE);

	private final ThreadLocal<Long> blockedTime = ThreadLocal.withInitial(() -> 0l);

	private final List<BlockingQueue<Object>> permits = new ArrayList<>();

	public RateLimiter() {
		permits.add(new ArrayBlockingQueue<>(50, true));
		permits.add(new ArrayBlockingQueue<>(50, true));
		permits.add(new ArrayBlockingQueue<>(100, true));
	}

	public void setThreadPriority(int priority) {
		MDC.put("threadPriority", String.valueOf(priority));
		threadPriority.set(priority);
	}

	public void clearThreadPriority() {
		threadPriority.remove();
		MDC.remove("threadPriority");
	}

	/**
	 * Returns and clears the time that this thread has spent blocked waiting
	 * for a permit.
	 */
	public long blockedTime() {
		Long time = blockedTime.get();
		blockedTime.remove();
		return time;
	}

	public void limitRate() throws InterruptedException {
		/*
		 * attempt to get a permit from a queue with the lowest possible
		 * priority. Make sure to go down to the allowed priority to
		 * correctly log blocking.
		 */
		int priority = threadPriority.get();
		for (int i = MAINTENANCE; i >= priority; i--) {
			Object permit = permits.get(i).poll();
			if (permit != null) {
				return;
			}
		}
		long startTime = System.currentTimeMillis();
		try {
			log.trace("Blocking");
			permits.get(priority).take();
		} finally {
			blockedTime.set(blockedTime.get() + (System.currentTimeMillis() - startTime));
			log.trace("Unblocked");
		}
	}

	/**
	 * This schedules adding permits at the official rate of the osu api.
	 */
	public void startSchedulingPermits(ScheduledExecutorService exec) {
		exec.scheduleAtFixedRate(this::addPermit, 0, 50, TimeUnit.MILLISECONDS);
	}

	public void addPermit() {
		/*
		 * Add a permit to the queue which has capacity with the highest
		 * priority. If all queues are full, add no
		 */
		for (BlockingQueue<Object> level : permits) {
			if (level.offer(PERMIT)) {
				return;
			}
		}
	}

	public List<Integer> getPermitCount() {
		return permits.stream().map(Collection::size).collect(toList());
	}
}

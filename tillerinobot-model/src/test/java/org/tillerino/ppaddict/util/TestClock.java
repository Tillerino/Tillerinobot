package org.tillerino.ppaddict.util;

import java.util.concurrent.atomic.AtomicLong;

public class TestClock implements Clock {
	private final AtomicLong time = new AtomicLong();

	@Override
	public long currentTimeMillis() {
		return time.get();
	}

	public void advanceBy(long millis) {
		time.addAndGet(millis);
	}

	public void set(long millis) {
		time.set(millis);
	}
}

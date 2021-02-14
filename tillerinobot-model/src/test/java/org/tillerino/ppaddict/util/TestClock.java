package org.tillerino.ppaddict.util;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import lombok.Getter;

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

	public static class Module extends AbstractModule {
		@Getter(onMethod = @__({@Provides, @Singleton}))
		private final TestClock clock = new TestClock();

		@Override
		protected void configure() {
			bind(Clock.class).to(TestClock.class);
		}
	}
}

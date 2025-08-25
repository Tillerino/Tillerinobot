package org.tillerino.ppaddict.util;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.NoArgsConstructor;

@Singleton
@NoArgsConstructor(onConstructor_ = @Inject)
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

	@dagger.Module
	public interface Module {
		@dagger.Binds
		Clock c(TestClock c);
  }
}

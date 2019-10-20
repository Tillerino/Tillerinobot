package org.tillerino.ppaddict.chat.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.tillerino.ppaddict.chat.impl.Bouncer.SemaphorePayload;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.TestClock;

public class BouncerTest {
	Clock clock = new TestClock();

	Bouncer bouncer = new Bouncer(clock);

	@Test
	public void testEnter() throws Exception {
		assertTrue(bouncer.tryEnter("nick", 1));
		assertThat(bouncer.get("nick")).contains(new SemaphorePayload(1, 0, null, 0, false));
	}

	@Test
	public void testRepeatedDenied() throws Exception {
		assertTrue(bouncer.tryEnter("it's a me", 1));
		assertFalse(bouncer.tryEnter("it's a me", 2));
		assertThat(bouncer.get("it's a me").get()).hasFieldOrPropertyWithValue("attemptsSinceEntered", 1);
	}

	@Test
	public void testOkAfterLeaving() throws Exception {
		assertTrue(bouncer.tryEnter("it's a me", 1));
		assertTrue(bouncer.exit("it's a me", 1));
		assertTrue(bouncer.tryEnter("it's a me", 2));
	}

	@Test
	public void testFalseExit() throws Exception {
		assertFalse(bouncer.exit("it's a me", 1));
		assertTrue(bouncer.tryEnter("it's a me", 1));
		assertFalse(bouncer.exit("it's a me", 2));
	}

	@Test
	public void testThreadSetting() throws Exception {
		bouncer.tryEnter("nick", 1);
		bouncer.setThread("nick", 1);
		assertThat(bouncer.getThread("nick")).contains(Thread.currentThread());
		assertFalse(bouncer.setThread("nick", 2));
		bouncer.clearThread("nick", 1);
		assertThat(bouncer.getThread("nick")).isEmpty();
	}
}

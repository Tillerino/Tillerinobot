package org.tillerino.ppaddict.util;

import org.junit.Test;

/**
 * It is super hard to write assertions here, so just check the output manually.
 * Nothing much should change in the code anyway.
 */
public class PhaseTimerTest {
	@Test
	public void testPhaseTimer() throws Exception {
		PhaseTimer phaseTimer = new PhaseTimer();
		Thread.sleep(10);
		phaseTimer.completePhase("Phase should be 10");
		Thread.sleep(20);
		phaseTimer.completePhase("Phase should be 20");
		phaseTimer.print();
	}

	@Test
	public void testTasks() throws Exception {
		PhaseTimer phaseTimer = new PhaseTimer();
		try (var _ = phaseTimer.pinToThread()) {
			try (var _ = PhaseTimer.timeTask("Task should be 10")) {
				Thread.sleep(10);
			}
			try (var _ = PhaseTimer.timeTask("Task should be 20")) {
				Thread.sleep(20);
			}
			phaseTimer.completePhase("Phase");
		}
		phaseTimer.print();
	}

	@Test
	public void repeatedTasks() throws Exception {
		PhaseTimer phaseTimer = new PhaseTimer();
		try (var _ = phaseTimer.pinToThread()) {
			try (var _ = PhaseTimer.timeTask("Task should be 2x10")) {
				Thread.sleep(10);
			}
			try (var _ = PhaseTimer.timeTask("Task should be 2x20")) {
				Thread.sleep(20);
			}
			try (var _ = PhaseTimer.timeTask("Task should be 2x10")) {
				Thread.sleep(10);
			}
			try (var _ = PhaseTimer.timeTask("Task should be 2x20")) {
				Thread.sleep(20);
			}
			phaseTimer.completePhase("Phase");
		}
		phaseTimer.print();
	}

	@Test
	public void nestedTasks() throws Exception {
		PhaseTimer phaseTimer = new PhaseTimer();
		try (var _ = phaseTimer.pinToThread()) {
			try (var _ = PhaseTimer.timeTask("Task should be 25 > 10")) {
				Thread.sleep(10);
				try (var _ = PhaseTimer.timeTask("Task should be 5")) {
					Thread.sleep(5);
				}
				try (var _ = PhaseTimer.timeTask("Task should be 5")) {
					Thread.sleep(5);
					try (var _ = PhaseTimer.timeTask("Task should be 5")) {
						Thread.sleep(5);
					}
				}
			}
			phaseTimer.completePhase("Phase");
		}
		phaseTimer.print();
	}
}
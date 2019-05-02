package org.tillerino.ppaddict.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements a runnable by calling a loop body over and over. The loop body is
 * implemented in {@link #loop()}.
 */
@Slf4j
public abstract class LoopingRunnable implements Runnable {
	@Override
	public final void run() {
		for (;;) {
			try {
				loop();
			} catch (InterruptedException e) {
				log.info("Interrupted. Stopping loop.");
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/**
	 * Is repeatedly called from {@link #run()} until an
	 * {@link InterruptedException} is thrown. All other exceptions are not
	 * caught.
	 *
	 * @throws InterruptedException to end the loop gracefully.
	 */
	protected abstract void loop() throws InterruptedException;
}

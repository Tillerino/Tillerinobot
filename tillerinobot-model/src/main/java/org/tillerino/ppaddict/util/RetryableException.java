package org.tillerino.ppaddict.util;

/**
 * An exception indicating that an action failed but can be retried.
 * Call {@link #waitBeforeRetry()} before retrying.
 */
public class RetryableException extends RuntimeException {
	private final int retryInMillis;

	public RetryableException(int retryInMillis) {
		this.retryInMillis = retryInMillis;
	}

	public void waitBeforeRetry() throws InterruptedException {
		if (retryInMillis > 0) {
			Thread.sleep(retryInMillis);
		}
	}
}

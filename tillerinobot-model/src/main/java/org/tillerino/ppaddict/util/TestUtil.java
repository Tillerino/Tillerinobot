package org.tillerino.ppaddict.util;

import java.net.BindException;
import java.util.Random;

public class TestUtil {
	public static <E extends Exception> void runOnRandomPort(int attempts, ThrowingIntConsumer<E> con) throws E, BindException {
		final Random rnd = new Random();
		for (int i = 1; i <= attempts; i++) {
			try {
				try {
					con.accept(1024 + rnd.nextInt(60000));
					break;
				} catch (RuntimeException e) {
					if (e.getCause() instanceof BindException) {
						throw (BindException) e.getCause();
					}
					throw e;
				}
			} catch (BindException e) {
				if (i == attempts) {
					throw e;
				}
			}
		}
	}

	public interface ThrowingIntConsumer<E extends Exception> {
		void accept(int i) throws E;
	}
}

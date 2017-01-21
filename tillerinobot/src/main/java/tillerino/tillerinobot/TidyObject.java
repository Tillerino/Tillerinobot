package tillerino.tillerinobot;

import java.lang.ref.WeakReference;

/**
 * Interface for classes which clean up after themselves.
 * 
 * @author Tillerino
 */
public interface TidyObject {
	/**
	 * Shutdown Thread for having a {@link TidyObject} tidy up.
	 * 
	 * @author Tillerino
	 */
	public static class ShutdownHook extends Thread {
		WeakReference<TidyObject> ref;

		private boolean added = false;
		private boolean removed = false;

		public ShutdownHook(TidyObject obj) {
			super();
			this.ref = new WeakReference<>(obj);
		}

		@Override
		public void run() {
			TidyObject now = ref.get();
			if (now != null) {
				now.tidyUp(true);
			}
		}

		public void add() {
			if (!added) {
				Runtime.getRuntime().addShutdownHook(this);
				added = true;
			}
		}

		public void remove(boolean fromShutdownHook) {
			if (fromShutdownHook) {
				/*
				 * calling this method during a shutdown, so we'll set removed
				 * to true if this method gets called again during the shutdown
				 */
				removed = true;
			} else if (added && !removed) {
				Runtime.getRuntime().removeShutdownHook(this);
				removed = true;
			}
		}
	}

	/**
	 * Close/shutdown/flush anything the object uses in this method.
	 * 
	 * @param fromShutdownHook
	 *            true if this method is called from a shutdown hook. if you
	 *            added a shutdown hook, you should not attempt to remove it if
	 *            this argument is true.
	 */
	void tidyUp(boolean fromShutdownHook);
}

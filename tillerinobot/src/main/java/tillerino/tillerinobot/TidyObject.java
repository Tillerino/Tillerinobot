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

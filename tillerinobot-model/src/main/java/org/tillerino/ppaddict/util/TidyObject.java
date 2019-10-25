package org.tillerino.ppaddict.util;

/**
 * Interface for classes which clean up after themselves.
 * 
 * @author Tillerino
 */
public interface TidyObject {
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

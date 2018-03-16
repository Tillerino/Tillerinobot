package tillerino.tillerinobot.util;

public interface IsMutable {
	/**
	 * Checks whether this object has been modified.
	 */
	boolean isModified();

	/**
	 * After this method has been called, calls to {@link #isModified()} will return
	 * <code>false</code> until the next modification of this object.
	 */
	void clearModified();
}

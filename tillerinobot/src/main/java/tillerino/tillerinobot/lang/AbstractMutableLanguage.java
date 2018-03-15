package tillerino.tillerinobot.lang;

import java.io.Serializable;

import tillerino.tillerinobot.util.IsMutable;

public abstract class AbstractMutableLanguage implements Language, IsMutable, Serializable {
	private static final long serialVersionUID = 1L;

	private transient boolean modified;

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void clearModified() {
		modified = false;
	}

	/**
	 * After this method has been called, calls to {@link #isModified()} will return
	 * true until {@link #clearModified()} is called.
	 */
	protected void registerModification() {
		modified = true;
	}
}

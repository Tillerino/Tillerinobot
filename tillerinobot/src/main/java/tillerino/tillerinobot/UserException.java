package tillerino.tillerinobot;

/**
 * This type of exception will be displayed to the user.
 * 
 * @author Tillerino
 */
public class UserException extends Exception {
	public static class QuietException extends UserException {
		private static final long serialVersionUID = 1L;

		public QuietException() {
			super(null);
		}
	}
	
	private static final long serialVersionUID = 1L;

	public UserException(String message) {
		super(message);
	}
	
	/**
	 * This type of exception is extremely rare in a sense that it won't occur
	 * again if the causing action is repeated.
	 * 
	 * @author Tillerino
	 */
	public static class RareUserException extends UserException {
		private static final long serialVersionUID = 1L;

		public RareUserException(String message) {
			super(message);
		}

	}
}
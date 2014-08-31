package tillerino.tillerinobot;


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
	
}
package tillerino.tillerinobot;

public interface BotRunner extends TidyObject, Runnable {
	boolean isConnected();

	/**
	 * Disconnects from the server in a soft manner. I.e. logs out / quits
	 * orderly. A "hard" disconnect would just close any open socket
	 * connections.
	 */
	void disconnectSoftly();
}
package tillerino.tillerinobot;

public interface BotRunner extends TidyObject, Runnable {
	boolean isConnected();

	void disconnectSoftly();
}
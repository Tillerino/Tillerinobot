package tillerino.tillerinobot.mbeans;

public interface PingerMXBean {
	void setPingDeathPending(boolean b);

	boolean isPingDeathPending();

	long getLastPing();

	long getLastPingDeath();
}

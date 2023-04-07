package org.tillerino.ppaddict.chat;

import javax.inject.Singleton;

import lombok.Data;

@Singleton
@Data
public class GameChatClientMetrics {
	private boolean isConnected;
	private long runningSince;
	private long lastPingDeath;
	private long lastInteraction;
	private long lastReceivedMessage;
	private long lastSentMessage;
	private long lastRecommendation;
}

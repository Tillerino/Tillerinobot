package org.tillerino.ppaddict.chat;

import javax.inject.Singleton;

import lombok.Data;

@Data
@Singleton
public class GameChatMetrics {
	private boolean isConnected;
	private long runningSince;
	private long lastPingDeath;
	private long lastInteraction;
	private long lastReceivedMessage;
	private long lastSentMessage;
	private long lastRecommendation;
	private long responseQueueSize;
	private long eventQueueSize;
}
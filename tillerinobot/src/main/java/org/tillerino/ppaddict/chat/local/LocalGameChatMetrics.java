package org.tillerino.ppaddict.chat.local;

import javax.inject.Singleton;

import lombok.Data;
import org.tillerino.ppaddict.chat.GameChatMetrics;

@Data
@Singleton
public class LocalGameChatMetrics implements GameChatMetrics {
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
package org.tillerino.ppaddict.chat.local;

import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.tillerino.ppaddict.chat.GameChatMetrics;

@Data
@NoArgsConstructor
@Builder(toBuilder = true) // so we can copy
@AllArgsConstructor(access = AccessLevel.PRIVATE) // for the builder
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
package org.tillerino.ppaddict.chat;

public interface GameChatMetrics {
    void setRunningSince(long runningSince);
    void setLastPingDeath(long lastPingDeath);
    void setLastInteraction(long lastInteraction);
    void setLastReceivedMessage(long lastReceivedMessage);
    void setLastSentMessage(long lastSentMessage);
    void setLastRecommendation(long lastRecommendation);
}

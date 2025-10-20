package org.tillerino.ppaddict.chat;

/** Sends messages to the live preview. */
public interface LiveActivity {
    void propagateReceivedMessage(@IRCName String ircUserName, long eventId);

    void propagateSentMessage(@IRCName String ircUserName, long eventId, Long ping);

    void propagateMessageDetails(long eventId, String text);
}

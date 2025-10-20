package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateMessage extends GameChatEvent {
    private final String message;

    public PrivateMessage(long eventId, @IRCName String nick, @MillisSinceEpoch long timestamp, String message) {
        super(eventId, nick, timestamp);
        this.message = message;
    }

    @Override
    public boolean isInteractive() {
        return true;
    }
}

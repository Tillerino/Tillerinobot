package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Sighted extends GameChatEvent {
    public Sighted(long eventId, @IRCName String nick, @MillisSinceEpoch long timestamp) {
        super(eventId, nick, timestamp);
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
}

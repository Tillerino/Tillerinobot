package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateAction extends GameChatEvent {
    private final String action;

    public PrivateAction(long eventId, @IRCName String nick, @MillisSinceEpoch long timestamp, String action) {
        super(eventId, nick, timestamp);
        this.action = action;
    }

    @Override
    public boolean isInteractive() {
        return true;
    }
}

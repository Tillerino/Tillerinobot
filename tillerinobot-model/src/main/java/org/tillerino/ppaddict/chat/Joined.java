package org.tillerino.ppaddict.chat;

import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Joined extends GameChatEvent {
	public Joined(long eventId, @IRCName String ircNick, @MillisSinceEpoch long timestamp) {
		super(eventId, ircNick, timestamp);
	}

	@Override
	public boolean isInteractive() {
		return false;
	}
}
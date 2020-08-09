package org.tillerino.ppaddict.chat;

import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Sighted extends GameChatEvent {
	public Sighted(long eventId, @IRCName String ircNick, @MillisSinceEpoch long timestamp) {
		super(eventId, ircNick, timestamp);
	}

	@Override
	public boolean isInteractive() {
		return false;
	}
}
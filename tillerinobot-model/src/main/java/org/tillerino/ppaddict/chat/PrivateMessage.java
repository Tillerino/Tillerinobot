package org.tillerino.ppaddict.chat;

import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateMessage extends GameChatEvent {
	private final String message;

	public PrivateMessage(long eventId, @IRCName String ircNick, @MillisSinceEpoch long timestamp, String message) {
		super(eventId, ircNick, timestamp);
		this.message = message;
	}

	@Override
	public boolean isInteractive() {
		return true;
	}
}
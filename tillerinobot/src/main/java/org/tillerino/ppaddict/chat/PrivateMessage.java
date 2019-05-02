package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import tillerino.tillerinobot.BotBackend.IRCName;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateMessage extends GameChatEvent {
	private final String message;

	public PrivateMessage(long eventId, @IRCName String ircNick, long timestamp, String message) {
		super(eventId, ircNick, timestamp);
		this.message = message;
	}

	@Override
	public boolean isInteractive() {
		return true;
	}
}
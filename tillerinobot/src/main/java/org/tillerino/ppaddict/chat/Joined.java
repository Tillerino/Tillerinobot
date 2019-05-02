package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import tillerino.tillerinobot.BotBackend.IRCName;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Joined extends GameChatEvent {
	public Joined(long eventId, @IRCName String ircNick, long timestamp) {
		super(eventId, ircNick, timestamp);
	}

	@Override
	public boolean isInteractive() {
		return false;
	}
}
package org.tillerino.ppaddict.chat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrivateAction extends GameChatEvent {
	private final String action;

	public PrivateAction(long eventId, @IRCName String ircNick, long timestamp, String action) {
		super(eventId, ircNick, timestamp);
		this.action = action;
	}

	@Override
	public boolean isInteractive() {
		return true;
	}
}
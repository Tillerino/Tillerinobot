package org.tillerino.ppaddict.chat;

import lombok.Data;
import lombok.Getter;

@Data
public abstract class GameChatEvent {
	private final long eventId;

	@Getter(onMethod = @__(@IRCName))
	private final @IRCName String nick;

	private final long timestamp;

	/**
	 * This is meta information which is collected throughout the phases. Since
	 * this information is mutable, we tuck it away in this field. This doesn't
	 * make the entire object immutable, but at least we have a clear overview
	 * of there the mutability lies.
	 */
	private final GameChatEventMeta meta = new GameChatEventMeta();

	protected GameChatEvent(long eventId, @IRCName String ircNick, long timestamp) {
		super();
		this.eventId = eventId;
		this.nick = ircNick;
		this.timestamp = timestamp;
	}

	public abstract boolean isInteractive();
}
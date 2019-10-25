package org.tillerino.ppaddict.chat;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GameChatEvent {
	private final long eventId;

	private final @IRCName String nick;

	private final long timestamp;

	/**
	 * This is meta information which is collected throughout the phases. Since
	 * this information is mutable, we tuck it away in this field. This doesn't
	 * make the entire object immutable, but at least we have a clear overview
	 * of there the mutability lies.
	 */
	private final GameChatEventMeta meta = new GameChatEventMeta();

	public abstract boolean isInteractive();
}
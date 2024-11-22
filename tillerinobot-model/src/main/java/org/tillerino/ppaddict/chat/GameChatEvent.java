package org.tillerino.ppaddict.chat;

import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = Id.MINIMAL_CLASS)
public abstract class GameChatEvent {
	private final long eventId;

	private final @IRCName String nick;

	@MillisSinceEpoch
	private final long timestamp;

	/**
	 * This is meta information which is collected throughout the phases. Since this
	 * information is mutable, we tuck it away in this field. This doesn't make the
	 * entire object immutable, but at least we have a clear overview of there the
	 * mutability lies.
	 */
	private GameChatEventMeta meta = new GameChatEventMeta();

	@JsonIgnore
	public abstract boolean isInteractive();

	@JsonIgnore
	public int getPriority() {
		if (isInteractive()) {
			return 3;
		} else if (this instanceof Joined) {
			return 2;
		}
		return 1;
	}

	public void completePhase(String name) {
		meta.getTimer().completePhase(name);
	}
}
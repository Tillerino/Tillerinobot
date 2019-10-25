package org.tillerino.ppaddict.chat;

import org.tillerino.ppaddict.util.MdcUtils.MdcSnapshot;

import lombok.Data;

/**
 * This is non-critical meta information of a {@link GameChatEvent} which is implemented as mutable.
 */
@Data
public class GameChatEventMeta {
	private long rateLimiterBlockedTime;

	private MdcSnapshot mdc;
}

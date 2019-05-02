package org.tillerino.ppaddict.chat;

import java.io.IOException;

public interface GameChatWriter {
	void message(String msg, GameChatEvent result) throws InterruptedException, IOException;

	void action(String msg, GameChatEvent result) throws InterruptedException, IOException;
}

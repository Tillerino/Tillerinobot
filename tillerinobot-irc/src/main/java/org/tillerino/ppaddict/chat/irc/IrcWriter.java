package org.tillerino.ppaddict.chat.irc;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.pircbotx.output.OutputUser;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IrcWriter implements GameChatWriter {
	private final Pinger pinger;

	private final AtomicReference<CloseableBot> bot = new AtomicReference<>();

	@Override
	public void message(String msg, GameChatEvent result) throws InterruptedException, IOException {
		send(result, output -> output.message(msg));
	}

	@Override
	public void action(String msg, GameChatEvent result) throws InterruptedException, IOException {
		send(result, output -> output.action(msg));
	}

	private void send(GameChatEvent event, Consumer<OutputUser> sender) throws IOException, InterruptedException {
		pinger.ping((CloseableBot) waitForBot());

		try {
			sender.accept(waitForBot().getUserChannelDao().getUser(event.getNick()).send());
		} catch (RuntimeException e) {
			if (e.getCause() instanceof InterruptedException) {
				// see org.pircbotx.output.OutputRaw.rawLine(String)
				throw (InterruptedException) e.getCause();
			}
			throw e;
		}
	}

	private CloseableBot waitForBot() throws InterruptedException {
		for (;;) {
			CloseableBot b = bot.get();
			if (b != null) {
				return b;
			}
			Thread.sleep(100);
		}
	}

	public void setBot(CloseableBot bot) {
		this.bot.set(bot);
	}
}

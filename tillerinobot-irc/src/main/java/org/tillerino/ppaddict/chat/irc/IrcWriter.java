package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.Result.err;
import static org.tillerino.ppaddict.util.Result.ok;

import java.io.IOException;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.pircbotx.output.OutputUser;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.IRCName;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;
import org.tillerino.ppaddict.util.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class IrcWriter implements GameChatWriter {
	private final Pinger pinger;

	private final AtomicReference<CloseableBot> bot = new AtomicReference<>();

	@Override
	public Result<Optional<Response>, Error> message(String msg, @IRCName String recipient) throws InterruptedException, IOException {
		return send(recipient, output -> output.message(msg));
	}

	@Override
	public Result<Optional<Response>, Error> action(String msg, @IRCName String recipient) throws InterruptedException, IOException {
		return send(recipient, output -> output.action(msg));
	}

	private Result<Optional<Response>, Error> send(@IRCName String recipient, Consumer<OutputUser> sender) {

		try {
			Result<Optional<Long>, Error> pingResponse = pinger.ping(waitForBot());
			if (pingResponse instanceof Result.Err<?, Error> err) {
				return Result.err(err.e());
			}

			sender.accept(waitForBot().getUserChannelDao().getUser(recipient).send());
			return ok(Optional.of(new Response(pingResponse.ok().flatMap(x -> x).orElse(null))));
		} catch (RuntimeException e) {
			if (e.getCause() instanceof InterruptedException) {
				Thread.currentThread().interrupt();
				// see org.pircbotx.output.OutputRaw.rawLine(String)
				return err(new Error.Retry(10000)); // wait for reboot
			}
			if (e.getMessage().equals("Not connected to server") || ExceptionUtils.getRootCause(e) instanceof SocketException) {
				// happens if the bot disconnects after waitForBot() finishes.
				// since we wait for the connection in waitForBot(), we can retry immediately.
				return err(new Error.Retry(0));
			}
			throw e;
		} catch (IOException e) {
			log.warn("Error while sending chat message", e);
			// since we wait for the connection in waitForBot(), we can retry immediately.
			return err(new Error.Retry(0));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return err(new Error.Retry(10000)); // wait for reboot
		}
	}

	private CloseableBot waitForBot() throws InterruptedException {
		for (;;) {
			CloseableBot b = bot.get();
			if (b != null && b.isConnected()) {
				return b;
			}
			Thread.sleep(100);
		}
	}

	public void setBot(CloseableBot bot) {
		this.bot.set(bot);
	}
}

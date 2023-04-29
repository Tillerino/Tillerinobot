package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.Result.ok;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.pircbotx.Utils;
import org.pircbotx.hooks.events.UnknownEvent;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.LoggingUtils;
import org.tillerino.ppaddict.util.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class Pinger {
	volatile String pingMessage = null;
	volatile CountDownLatch pingLatch = null;

	final GameChatClientMetrics botInfo;

	final Clock clock;

	final AtomicInteger pingCalls = new AtomicInteger();

	Result<Optional<Long>, GameChatWriter.Error> ping(CloseableBot bot) throws IOException, InterruptedException {
		if (pingCalls.incrementAndGet() % 10 != 0) {
			return ok(Optional.empty());
		}

		if(!bot.isConnected()) {
			throw new IOException("bot is no longer connected");
		}

		long time = clock.currentTimeMillis();

		synchronized (this) {
			pingLatch = new CountDownLatch(1);
			pingMessage = LoggingUtils.getRandomString(16);
		}

		log.debug("PING {}", pingMessage);
		Utils.sendRawLineToServer(bot, "PING " + pingMessage);

		if(!pingLatch.await(10, TimeUnit.SECONDS)) {
			botInfo.setLastPingDeath(clock.currentTimeMillis());
			return Result.err(new GameChatWriter.Error.PingDeath(10000));
		}

		long ping = clock.currentTimeMillis() - time;
		return ok(Optional.of(ping));
	}

	public void handleUnknownEvent(@SuppressWarnings("rawtypes") UnknownEvent event) {
		synchronized(this) {
			if (pingMessage == null)
				return;

			boolean contains = event.getLine().contains(" PONG ");
			boolean endsWith = event.getLine().endsWith(pingMessage);
			if (contains && endsWith) {
				log.debug("PONG {}", pingMessage);
				pingLatch.countDown();
			} else if(contains) {
				log.warn("unknown pong: {}", event.getLine());
			}
		}
	}
}

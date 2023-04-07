package org.tillerino.ppaddict.chat.irc;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.pircbotx.Utils;
import org.pircbotx.hooks.events.UnknownEvent;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl.CloseableBot;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.LoggingUtils;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Pinger {
	volatile String pingMessage = null;
	volatile CountDownLatch pingLatch = null;

	final GameChatClientMetrics botInfo;

	final Clock clock;

	final AtomicInteger pingCalls = new AtomicInteger();

	void ping(CloseableBot bot) throws IOException, InterruptedException {
		if (pingCalls.incrementAndGet() % 10 != 0) {
			return;
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
			MDC.put(MdcUtils.MDC_PING, 10000 + "");
			botInfo.setLastPingDeath(clock.currentTimeMillis());
			throw new IOException("ping timed out");
		}

		long ping = clock.currentTimeMillis() - time;
		MDC.put(MdcUtils.MDC_PING, ping + "");
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

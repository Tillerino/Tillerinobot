package org.tillerino.ppaddict.chat.irc;

import static java.util.function.Function.identity;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.annotation.CheckForNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitRpc;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.util.Clock;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	public static void main(String[] args) throws Exception {
		log.info("starting irc");
		Bot bot = createBotRunner();
		Undertow httpServer = Undertow.builder()
			.addHttpListener(8080, "0.0.0.0")
			.setHandler(exchange -> {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("Hello World");
			})
			.setIoThreads(1)
			.setWorkerThreads(1)
			.build();
		httpServer.start();
		if (bot != null) {
			ExecutorService exec = Executors.newCachedThreadPool();
			exec.submit(RabbitRpc.handleRemoteCalls(bot.conn(), GameChatClient.class, bot.runner(), new GameChatClient.Error.Unknown())::mainloop);
			exec.submit(RabbitRpc.handleRemoteCalls(bot.conn(), GameChatWriter.class, bot.runner().getWriter(), new GameChatWriter.Error.Unknown())::mainloop);
			exec.submit(bot.runner());
		} else {
			log.info("IRC server not configured. Only starting HTTP.");
		}
	}

	private static @CheckForNull Bot createBotRunner() throws IOException, TimeoutException {
		String server = null;
		try {
			server = env("TILLERINOBOT_IRC_SERVER", identity(), null);
		} catch (NoSuchElementException e) {
			return null;
		}

		int port = env("TILLERINOBOT_IRC_PORT", Integer::valueOf, null);
		String nickname = env("TILLERINOBOT_IRC_NICKNAME", identity(), null);
		String password = env("TILLERINOBOT_IRC_PASSWORD", identity(), null);
		String autojoinChannel = env("TILLERINOBOT_IRC_AUTOJOIN", identity(), null);
		boolean silent = env("TILLERINOBOT_IGNORE", Boolean::valueOf, null);

		Rabbit rabbit = createRabbit();

		BotRunnerImpl botRunner = new BotRunnerImpl(server, port, nickname, password, autojoinChannel, silent, rabbit.queue(), Clock.system());
		return new Bot(rabbit.conn(), botRunner);
	}

	private static Rabbit createRabbit() throws IOException, TimeoutException {
		String rabbitHost = env("RABBIT_HOST", identity(), "rabbitmq");
		int rabbitPort = env("RABBIT_PORT", Integer::valueOf, 5672);

		ConnectionFactory connectionFactory = RabbitMqConfiguration.connectionFactory(rabbitHost, rabbitPort);
		Connection connection = connectionFactory.newConnection("tillerinobot-irc");

		RemoteEventQueue downStream = RabbitMqConfiguration.externalEventQueue(connection);
		downStream.setup();

		return new Rabbit(connection, downStream);
	}

	static <T> T env(String name, Function<String, T> parser, T defaultValue) {
		Optional<T> optional = Optional.ofNullable(System.getenv(name)).map(parser);
		if (defaultValue != null) {
			return optional.orElse(defaultValue);
		}
		return optional.orElseThrow(() -> new NoSuchElementException("Need to configure environment variable " + name));
	}

	record Bot(Connection conn, BotRunnerImpl runner) { }

	record Rabbit(Connection conn, RemoteEventQueue queue) { }
}

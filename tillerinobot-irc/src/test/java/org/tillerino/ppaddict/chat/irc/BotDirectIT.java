package org.tillerino.ppaddict.chat.irc;

import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.AfterClass;

import java.net.InetSocketAddress;

import static org.tillerino.ppaddict.chat.irc.NgircdContainer.NGIRCD;
import static org.tillerino.ppaddict.rabbit.RabbitMqContainer.RABBIT_MQ;

/**
 * Does not use the container, but starts the bot directly.
 * We do this so we can measure coverage.
 * It runs as an integration test as well so that we can recycle containers.
 */
public class BotDirectIT extends BotIT {
	private static Main.RunningMain main;

	@Override
	protected void startBot() throws Exception {
		if (main != null) {
			return;
		}
		Thread.sleep(3000);
		Main.IrcConfig ircConfig = new Main.IrcConfig(NGIRCD.getHost(), NGIRCD.getMappedPort(6667), "tillerinobot", "", "#osu", false);
		main = Main.main(ircConfig, new Main.RabbitConfig(RABBIT_MQ.getHost(), RABBIT_MQ.getAmqpPort()), new Main.UndertowConfig(0));
		RestAssured.baseURI = "http://localhost:" + ((InetSocketAddress) main.httpServer().getListenerInfo().get(0).getAddress()).getPort();
	}

	@Override
	protected void stopBot() throws Exception {
		doStopBot();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		doStopBot();
	}

	private static void doStopBot() throws Exception {
		main.bot().conn().close();
		main.bot().runner().tidyUp(false);
		main.exec().shutdownNow();
		Awaitility.await().until(() -> main.exec().isTerminated());
		main.httpServer().stop();
		main = null;
	}

	@Override
	public void livenessReactsToNgircd() throws Exception {
		// do nothing because the container will not expose the same port the second time
	}

	@Override
	public void livenessReactsToRabbit() throws Exception {
		// do nothing because the container will not expose the same port the second time
	}
}

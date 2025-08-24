package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.io.File;

import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;
import org.tillerino.ppaddict.util.CustomTestContainer;

public class IrcContainer {
	private static final ImageFromDockerfile base = new ImageFromDockerfile()
			.withFileFromFile(".", new File("../../Tillerinobot/tillerinobot-irc"));

	public static final CustomTestContainer TILLERINOBOT_IRC = new CustomTestContainer(base)
		.withNetwork(NETWORK)
		.withExposedPorts(8080)
		.waitingFor(new HttpWaitStrategy().forPort(8080).forPath("/live"))
		.withEnv("TILLERINOBOT_IRC_SERVER", "irc")
		.withEnv("TILLERINOBOT_IRC_PORT", "6667")
		.withEnv("TILLERINOBOT_IRC_NICKNAME", "tillerinobot")
		.withEnv("TILLERINOBOT_IRC_PASSWORD", "")
		.withEnv("TILLERINOBOT_IRC_AUTOJOIN", "#osu")
		.withEnv("TILLERINOBOT_IGNORE", "false")
		.withEnv("RABBIT_VHOST", RabbitMqContainer.getVirtualHost())
		.logging("IRC");

	static {
		NgircdContainer.NGIRCD.start();
		RabbitMqContainer.start();
		TILLERINOBOT_IRC.start();
	}
}

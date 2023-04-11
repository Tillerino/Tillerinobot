package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.io.File;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class IrcContainer {
	private static final GenericContainer TILLERINOBOT_IRC = new GenericContainer<>(new ImageFromDockerfile()
			.withFileFromFile("Dockerfile", new File("../tillerinobot-irc/Dockerfile"))
			.withFileFromFile("target", new File("../tillerinobot-irc/target")))
		// we set a fixed container name to make it more debuggable
		.withCreateContainerCmdModifier(cmd -> cmd.withName("tillerinobot-irc"))
		.withNetwork(NETWORK)
		.withExposedPorts(8080)
		.waitingFor(new HttpWaitStrategy().forPort(8080).forPath("/ready"))
		.withLogConsumer(frame -> System.out.println("IRC: " + frame.getUtf8String().trim()));

	static {
		TILLERINOBOT_IRC.start();
	}
}

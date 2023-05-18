package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;
import org.tillerino.ppaddict.util.CustomTestContainer;

public class LiveContainer {
	private static final ImageFromDockerfile POTENTIALLY_PREBUILT = Files
			.isExecutable(Paths.get("../tillerinobot-live/target/release/main"))
					? new ImageFromDockerfile().withFileFromPath("target/release/main",
							Paths.get("../tillerinobot-live/target/release/main"))
					: new ImageFromDockerfile();

	private static final CustomTestContainer LIVE = new CustomTestContainer(POTENTIALLY_PREBUILT
			// move to parent so we can use this in multiple modules
			// make sure to keep this aligned with dockerignore
			.withFileFromPath("Dockerfile", Paths.get("../tillerinobot-live/Dockerfile"))
			.withFileFromPath("src/main/rust", Paths.get("../tillerinobot-live/src/main/rust"))
			.withFileFromPath("Cargo.toml", Paths.get("../tillerinobot-live/Cargo.toml"))
			.withFileFromPath("Cargo.lock", Paths.get("../tillerinobot-live/Cargo.lock")))
			// accesing this static variable will make sure that RabbitMQ is started
			.withEnv("RABBIT_VHOST", RabbitMqContainer.getVirtualHost())
			.withNetwork(NETWORK)
			.withExposedPorts(8080)
			.waitingFor(Wait.forHttp("/ready").forStatusCode(200))
			.withCreateContainerCmdModifier(cmd -> cmd.withMemory(16 * 1024 * 1024L).withMemorySwap(16 * 1024 * 1024L))
			.logging("LIVE");

	static {
		LIVE.start();
	}

	public static CustomTestContainer getLive() {
		return LIVE;
	}
}

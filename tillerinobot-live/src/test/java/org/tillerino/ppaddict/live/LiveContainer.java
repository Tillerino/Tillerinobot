package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class LiveContainer {
	private static final ImageFromDockerfile POTENTIALLY_PREBUILT = Files
			.isExecutable(Paths.get("../tillerinobot-live/target/release/main"))
					? new ImageFromDockerfile().withFileFromPath("target/release/main",
							Paths.get("../tillerinobot-live/target/release/main"))
					: new ImageFromDockerfile();

	private static final GenericContainer LIVE = new GenericContainer(POTENTIALLY_PREBUILT
			// move to parent so we can use this in multiple modules
			// make sure to keep this aligned with dockerignore
			.withFileFromPath("Dockerfile", Paths.get("../tillerinobot-live/Dockerfile"))
			.withFileFromPath("src/main/rust", Paths.get("../tillerinobot-live/src/main/rust"))
			.withFileFromPath("Cargo.toml", Paths.get("../tillerinobot-live/Cargo.toml"))
			.withFileFromPath("Cargo.lock", Paths.get("../tillerinobot-live/Cargo.lock"))
			.withFileFromPath("rust-toolchain", Paths.get("../tillerinobot-live/rust-toolchain")))
			// accesing this static variable will make sure that RabbitMQ is started
			.withNetwork(NETWORK)
			.withExposedPorts(8080)
			.waitingFor(Wait.forHttp("/ready").forStatusCode(200))
			.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
					cmd.withMemory(106 * 1024 * 1024L)
							.withMemorySwap(106 * 1024 * 1024L))
//			.withLogConsumer((Consumer<OutputFrame>) frame -> System.out.println(frame.getUtf8String().trim()))
			;

	static {
		LIVE.start();
	}

	public static GenericContainer getLive() {
		return LIVE;
	}
}

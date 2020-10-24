package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.nio.file.Paths;
import java.util.function.Consumer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class LiveContainer {
	private static final GenericContainer LIVE = new GenericContainer(new ImageFromDockerfile()
			// move to parent so we can use this in multiple modules
			.withFileFromPath(".", Paths.get("../tillerinobot-live/.")))
			// accesing this static variable will make sure that RabbitMQ is started
			.withNetwork(NETWORK)
			.withExposedPorts(8080)
			.waitingFor(Wait.forHttp("/ready").forStatusCode(200))
			.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
					cmd.withMemory(100 * 1024 * 1024L)
							.withMemorySwap(100 * 1024 * 1024L))
//			.withLogConsumer((Consumer<OutputFrame>) frame -> System.out.println(frame.getUtf8String().trim()))
			;

	static {
		LIVE.start();
	}

	public static GenericContainer getLive() {
		return LIVE;
	}
}

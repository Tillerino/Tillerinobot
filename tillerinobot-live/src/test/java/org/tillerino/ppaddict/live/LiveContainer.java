package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.live.RabbitMqContainer.NETWORK;

import java.nio.file.Paths;
import java.util.function.Consumer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class LiveContainer {
	private static final GenericContainer LIVE = new GenericContainer(new ImageFromDockerfile()
			.withFileFromPath(".", Paths.get(".")))
			// accesing this static variable will make sure that RabbitMQ is started
			.withNetwork(NETWORK)
			.waitingFor(Wait.forHttp("/ready").forStatusCode(200))
			.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
					cmd.withMemory(64 * 1024 * 1024L)
							.withMemorySwap(64 * 1024 * 1024L))
			.withLogConsumer((Consumer<OutputFrame>) frame -> System.out.println(frame.getUtf8String().trim()));

	static {
		LIVE.start();
	}

	public static GenericContainer getLive() {
		return LIVE;
	}
}

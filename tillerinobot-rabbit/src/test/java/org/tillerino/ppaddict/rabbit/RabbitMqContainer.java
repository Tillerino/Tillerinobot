package org.tillerino.ppaddict.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.tillerino.ppaddict.util.ReusableContainerInitializer;

public class RabbitMqContainer {
	private static final String VIRTUAL_HOST = UUID.randomUUID().toString();


	private static final Logger logger = LoggerFactory.getLogger("RABBIT");
	private static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer()
		.withNetwork(NETWORK)
		.withNetworkAliases("rabbitmq")
		.withReuse(true)
		.withLogConsumer(new Consumer<OutputFrame>() {
			// this logger makes sure that we don't output old logs from the reusable container.

			boolean trip = false;
			String startup = LocalDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
			@Override
			public void accept(OutputFrame frame) {
				String line = frame.getUtf8StringWithoutLineEnding();

				// startsWith 20 checks if it starts with a timestamp. this will work until 2100 :D
				if (!trip && line.startsWith("20") && line.compareTo(startup) >= 0) {
					trip = true;
				}
				if (trip) {
					logger.info(line);
				}
			}
		});

	private static final ReusableContainerInitializer<RabbitMQContainer> initializer = new ReusableContainerInitializer<>(
		RABBIT_MQ, container ->
			assertThat(container.execInContainer("rabbitmqadmin", "declare", "vhost", "name=" + VIRTUAL_HOST)
					.getExitCode())
				.isZero());

	static {
		start();
	}

	public synchronized static void start() {
		initializer.start();
	}

	public static String getHost() {
		return initializer.start().getHost();
	}

	public static int getAmqpPort() {
		return initializer.start().getAmqpPort();
	}

	public static String getVirtualHost() {
		return VIRTUAL_HOST;
	}

	public static void stop() {
		RABBIT_MQ.stop();
	}
}

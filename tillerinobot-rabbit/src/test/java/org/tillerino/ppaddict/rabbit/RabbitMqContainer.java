package org.tillerino.ppaddict.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;


public class RabbitMqContainer {
	private static final String VIRTUAL_HOST = UUID.randomUUID().toString();
	private static String virtualHostAddedToId = null;

	private static final Logger logger = LoggerFactory.getLogger("RABBIT");
	public static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer()
		.withNetwork(NETWORK)
		.withNetworkAliases("rabbitmq")
		.withReuse(true)
		.withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()));

	static {
		start();
	}

	public synchronized static void start() {
		RABBIT_MQ.start();
		if (!Objects.equals(virtualHostAddedToId, RABBIT_MQ.getContainerId())) {
			logger.info("Adding virtual host " + VIRTUAL_HOST);
			try {
				int exitCode = RABBIT_MQ.execInContainer("rabbitmqadmin", "declare", "vhost", "name=" + VIRTUAL_HOST)
					.getExitCode();
				assertThat(exitCode).isZero();
				virtualHostAddedToId = RABBIT_MQ.getContainerId();
			} catch (Exception e) {
				logger.error("Error creating virtual host", e);
			}
			logger.info("RabbitMQ admin at " + RABBIT_MQ.getHttpUrl());
		}
	}

	public static String getHost() {
		start();
		return RABBIT_MQ.getHost();
	}

	public static int getAmqpPort() {
		start();
		return RABBIT_MQ.getAmqpPort();
	}

	public static String getVirtualHost() {
		return VIRTUAL_HOST;
	}

	public static void stop() {
		RABBIT_MQ.stop();
	}
}

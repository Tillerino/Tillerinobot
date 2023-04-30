package org.tillerino.ppaddict.rabbit;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;


public class RabbitMqContainer {
	private static final Logger logger = LoggerFactory.getLogger("RABBIT");
	public static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer()
			.withNetwork(NETWORK)
			.withNetworkAliases("rabbitmq")
			.withLogConsumer(frame -> logger.info(frame.getUtf8StringWithoutLineEnding()));

	static {
		RABBIT_MQ.start();
		logger.info("RabbitMQ admin at " + RABBIT_MQ.getHttpUrl());
	}

	public static RabbitMQContainer getRabbitMq() {
		return RABBIT_MQ;
	}
}

package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import org.testcontainers.containers.RabbitMQContainer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqContainer {
	private static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer()
			.withNetwork(NETWORK)
			.withNetworkAliases("rabbitmq");

	static {
		RABBIT_MQ.start();
		log.info("RabbitMQ admin at " + RABBIT_MQ.getHttpUrl());
	}

	public static RabbitMQContainer getRabbitMq() {
		return RABBIT_MQ;
	}
}

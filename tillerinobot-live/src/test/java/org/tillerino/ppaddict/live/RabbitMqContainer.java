package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import org.testcontainers.containers.RabbitMQContainer;

public class RabbitMqContainer {
	private static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer()
			.withNetwork(NETWORK)
			.withNetworkAliases("rabbitmq");

	static {
		RABBIT_MQ.start();
	}

	public static RabbitMQContainer getRabbitMq() {
		return RABBIT_MQ;
	}
}

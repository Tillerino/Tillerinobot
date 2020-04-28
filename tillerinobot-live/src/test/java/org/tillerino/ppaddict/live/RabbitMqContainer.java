package org.tillerino.ppaddict.live;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;

public class RabbitMqContainer {
	static final Network NETWORK = Network.newNetwork();

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

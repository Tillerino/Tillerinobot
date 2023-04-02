package org.tillerino.ppaddict.rabbit;

import org.junit.rules.ExternalResource;

import com.rabbitmq.client.Connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqContainerConnection extends ExternalResource {
	@Getter
	private Connection connection;

	@Override
	protected void before() throws Throwable {
		connection = RabbitMqConfiguration
				.connectionFactory("" + RabbitMqContainer.getRabbitMq().getContainerIpAddress(),
						RabbitMqContainer.getRabbitMq().getMappedPort(5672))
				.newConnection("test");
	}

	@Override
	protected void after() {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				// we don't care
			}
		}
	}
}
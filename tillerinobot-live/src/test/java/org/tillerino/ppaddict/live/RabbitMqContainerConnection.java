package org.tillerino.ppaddict.live;

import org.junit.rules.ExternalResource;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqContainerConnection extends ExternalResource {
	private Connection connection;

	@Getter
	private Channel channel;

	@Override
	protected void before() throws Throwable {
		connection = RabbitMqConfiguration
				.connectionFactory("" + RabbitMqContainer.getRabbitMq().getContainerIpAddress(),
						RabbitMqContainer.getRabbitMq().getMappedPort(5672))
				.newConnection("test");
		channel = connection.createChannel();
	}

	@Override
	protected void after() {
		if (channel != null) {
			try {
				channel.close();
			} catch (Exception e) {
				// we don't care
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				// we don't care
			}
		}
	}
}
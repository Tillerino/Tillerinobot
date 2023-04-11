package org.tillerino.ppaddict.rabbit;

import java.util.concurrent.ExecutorService;

import org.junit.rules.ExternalResource;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqContainerConnection extends ExternalResource {
	@Getter
	private Connection connection;

	private final ExecutorService sharedExecutorService;

	@Override
	protected void before() throws Throwable {
		ConnectionFactory connectionFactory = RabbitMqConfiguration.connectionFactory(
			"" + RabbitMqContainer.getRabbitMq().getHost(),
			RabbitMqContainer.getRabbitMq().getMappedPort(5672));
		if (sharedExecutorService != null) {
			connectionFactory.setSharedExecutor(sharedExecutorService);
		}

		connection = connectionFactory.newConnection("test");
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
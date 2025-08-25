package org.tillerino.ppaddict.rabbit;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqContainerConnection implements BeforeEachCallback, AfterEachCallback {
	@Getter
	private Connection connection;

	private final ExecutorService sharedExecutorService;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		ConnectionFactory connectionFactory = RabbitMqConfiguration.connectionFactory(
			RabbitMqContainer.getHost(), RabbitMqContainer.getAmqpPort(), RabbitMqContainer.getVirtualHost());
		if (sharedExecutorService != null) {
			connectionFactory.setSharedExecutor(sharedExecutorService);
		}

		connection = connectionFactory.newConnection("test");
	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				// we don't care
			}
		}
	}
}
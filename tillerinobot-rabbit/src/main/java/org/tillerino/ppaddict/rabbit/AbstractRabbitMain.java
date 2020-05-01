package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Base class for main functions that require a RabbitMQ connection
 */
public class AbstractRabbitMain {
	private final Logger log;
	protected final ConnectionFactory rabbitFactory;

	private Connection rabbitConnection;
	private Channel rabbitChannel;

	public AbstractRabbitMain(Logger log, String rabbitHost, int rabbitPort) {
		this.log = log;
		rabbitFactory = RabbitMqConfiguration.connectionFactory(rabbitHost, rabbitPort);
	}

	protected void start(String connectionName) throws IOException, TimeoutException {
		log.info("Connecting to RabbitMQ {}:{}", rabbitFactory.getHost(), rabbitFactory.getPort());
		rabbitConnection = rabbitFactory.newConnection(connectionName);
		log.info("Connected  to RabbitMQ {}:{}", rabbitFactory.getHost(), rabbitFactory.getPort());
		log.info("Opening RabbitMQ channel");
		rabbitChannel = rabbitConnection.createChannel();
		log.info("Opened  RabbitMQ channel");
	}

	protected void stop() throws IOException, TimeoutException {
		if (rabbitChannel != null) {
			try {
				rabbitChannel.close();
			} catch (Exception e) {
				log.error("Error closing RabbitMQ channel", e);
			}
		}
		if (rabbitConnection != null) {
			try {
				rabbitConnection.close();
			} catch (Exception e) {
				log.error("Error closing RabbitMQ connection", e);
			}
		}
	}

	protected Channel getRabbitChannel() {
		if (rabbitChannel == null) {
			throw new IllegalStateException("RabbitMQ connection not started");
		}
		return rabbitChannel;
	}
}

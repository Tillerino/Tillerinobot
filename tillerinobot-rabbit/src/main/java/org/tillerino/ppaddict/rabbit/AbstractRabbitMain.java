package org.tillerino.ppaddict.rabbit;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private ExecutorService rabbitExecutor;

	public AbstractRabbitMain(Logger log, String rabbitHost, int rabbitPort) {
		this.log = log;
		rabbitFactory = RabbitMqConfiguration.connectionFactory(rabbitHost, rabbitPort);
	}

	protected synchronized void start(String connectionName) throws IOException, TimeoutException {
		rabbitExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "RabbitMQ task executor"));
		log.info("Connecting to RabbitMQ {}:{}", rabbitFactory.getHost(), rabbitFactory.getPort());
		rabbitConnection = rabbitFactory.newConnection(rabbitExecutor, connectionName);
		log.info("Connected  to RabbitMQ {}:{}", rabbitFactory.getHost(), rabbitFactory.getPort());
		log.info("Opening RabbitMQ channel");
		rabbitChannel = rabbitConnection.createChannel();
		log.info("Opened  RabbitMQ channel");
	}

	protected synchronized void stop() {
		if (rabbitChannel != null) {
			try {
				rabbitChannel.close();
				rabbitChannel = null;
			} catch (Exception e) {
				log.error("Error closing RabbitMQ channel", e);
			}
		}
		if (rabbitConnection != null) {
			try {
				rabbitConnection.close();
				rabbitConnection = null;
			} catch (Exception e) {
				log.error("Error closing RabbitMQ connection", e);
			}
		}
		if (rabbitExecutor != null) {
			rabbitExecutor.shutdown();
			rabbitExecutor = null;
		}
	}

	protected synchronized Channel getRabbitChannel() {
		if (rabbitChannel == null) {
			throw new IllegalStateException("RabbitMQ connection not started or already shut down");
		}
		return rabbitChannel;
	}
}

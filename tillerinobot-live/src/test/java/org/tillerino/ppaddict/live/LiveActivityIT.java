package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.live.RabbitMqContainer.getRabbitMq;

import java.net.BindException;
import java.util.Random;

import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class LiveActivityIT extends AbstractLiveActivityEndpointTest {
	private LiveMain main;
	private Connection connection;
	private Channel channel;
	private RemoteLiveActivity push;
	private int port;

	@Override
	public void setUp() throws Exception {
		ConnectionFactory connectionFactory = RabbitMqConfiguration.connectionFactory(getRabbitMq().getContainerIpAddress(), getRabbitMq().getAmqpPort());
		connection = connectionFactory.newConnection();
		channel = connection.createChannel();
		push = RabbitMqConfiguration.liveActivity(channel);
		startOnFreePort();
		super.setUp();
	}

	/**
	 * I couldn't find out how to start Undertow on an unspecified free port so we're just rolling the dice a few times.
	 */
	private void startOnFreePort() throws Exception {
		final Random rnd = new Random();
		for (int i = 1; i <= 10; i++) {
			port = 1024 + rnd.nextInt(60000);
			main = new LiveMain(port, getRabbitMq().getContainerIpAddress(), getRabbitMq().getAmqpPort());
			try {
				try {
					main.start();
				} catch (RuntimeException e) {
					if (e.getCause() instanceof BindException) {
						throw (BindException) e.getCause();
					}
				}
			} catch (BindException e) {
				if (i == 10) {
					throw e;
				}
				continue;
			}
			break;
		}
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		main.stop();
		channel.close();
		connection.close();
	}

	@Override
	protected int port() {
		return port;
	}

	@Override
	protected String host() {
		return "localhost";
	}

	@Override
	protected LiveActivityEndpoint impl() {
		return main.live;
	}

	@Override
	protected LiveActivity push() {
		return push;
	}
}

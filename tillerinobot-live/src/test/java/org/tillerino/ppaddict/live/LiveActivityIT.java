package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.live.RabbitMqContainer.getRabbitMq;

import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;
import org.tillerino.ppaddict.util.TestUtil;

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
		TestUtil.runOnRandomPort(10, p -> {
			port = p;
			main = new LiveMain(port, getRabbitMq().getContainerIpAddress(), getRabbitMq().getAmqpPort());
			main.start("tillerinobot-live");
		});
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

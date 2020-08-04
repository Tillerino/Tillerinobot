package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.live.RabbitMqContainer.getRabbitMq;

import org.junit.ClassRule;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;
import org.tillerino.ppaddict.util.TestUtil;

public class LiveActivityIT extends AbstractLiveActivityEndpointTest {

	@ClassRule
	public static RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection();

	private LiveMain main;
	private RemoteLiveActivity push;
	private int port;

	@Override
	public void setUp() throws Exception {
		push = RabbitMqConfiguration.liveActivity(rabbit.getChannel());
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

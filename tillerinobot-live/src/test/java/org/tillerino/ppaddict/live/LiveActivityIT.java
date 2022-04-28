package org.tillerino.ppaddict.live;

import org.junit.ClassRule;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;

public class LiveActivityIT extends AbstractLiveActivityEndpointTest {

	@ClassRule
	public static RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection();

	private RemoteLiveActivity push;

	@Override
	public void setUp() throws Exception {
		push = RabbitMqConfiguration.liveActivity(rabbit.getConnection());
		super.setUp();
	}
	@Override
	protected int port() {
		return LiveContainer.getLive().getMappedPort(8080);
	}

	@Override
	protected String host() {
		return LiveContainer.getLive().getHost();
	}

	@Override
	protected LiveActivity push() {
		return push;
	}
}

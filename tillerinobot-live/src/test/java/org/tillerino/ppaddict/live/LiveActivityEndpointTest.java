package org.tillerino.ppaddict.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.live.LiveActivityEndpoint.anonymizeHashCode;

import org.junit.Rule;
import org.junit.Test;
import org.tillerino.ppaddict.chat.LiveActivity;

public class LiveActivityEndpointTest extends AbstractLiveActivityEndpointTest {

	@Test
	public void testAnonymize() {
		// deterministic
		assertThat(anonymizeHashCode("a", "b")).isEqualTo(anonymizeHashCode("a", "b"));
		// change salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("a", "c"));
		// change object
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("d", "b"));
		// swap object and salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("b", "a"));
	}

	@Rule
	public final JettyWebsocketServerResource webSocketServer = new JettyWebsocketServerResource("localhost", 0);

	private final LiveActivityEndpoint liveActivity = new LiveActivityEndpoint();

	@Override
	public void setUp() throws Exception {
		webSocketServer.addEndpoint(liveActivity);
		super.setUp();
	}

	@Override
	protected int port() {
		return webSocketServer.getPort();
	}

	@Override
	protected String host() {
		return "localhost";
	}

	@Override
	protected LiveActivityEndpoint impl() {
		return liveActivity;
	}

	@Override
	protected LiveActivity push() {
		return liveActivity;
	}
}

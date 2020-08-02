package org.tillerino.ppaddict;

import javax.websocket.DeploymentException;

import org.junit.Rule;
import org.tillerino.ppaddict.chat.irc.EmbeddedIrcServerRule;
import org.tillerino.ppaddict.live.JettyWebsocketServerResource;
import org.tillerino.ppaddict.live.LiveActivityEndpoint;

import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;

/**
 * This test starts an embedded IRC server, mocks a backend and requests
 * recommendations from multiple users in parallel.
 */
@Slf4j
public class FullBotTest extends AbstractFullBotTest {
	public FullBotTest() {
		super(log);
	}

	@Rule
	public final EmbeddedIrcServerRule server = new EmbeddedIrcServerRule();

	@Rule
	public final JettyWebsocketServerResource webSocket = new JettyWebsocketServerResource("localhost", 0);


	@Override
	protected String getWsUrl(Injector injector) throws DeploymentException {
		webSocket.addEndpoint(injector.getInstance(LiveActivityEndpoint.class));
		return "ws://localhost:" + webSocket.getPort() + "/live/v0";
	}

	@Override
	Injector createInjector() {
		return Guice.createInjector(new FullBotConfiguration(ircHost(), ircPort(), exec));
	}

	@Override
	protected int ircPort() {
		return server.getPort();
	}

	@Override
	protected String ircHost() {
		return "localhost";
	}
}

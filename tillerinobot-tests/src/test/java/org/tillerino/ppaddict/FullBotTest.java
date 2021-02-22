package org.tillerino.ppaddict;

import static org.tillerino.ppaddict.live.LiveContainer.getLive;

import javax.websocket.DeploymentException;

import org.junit.Rule;
import org.tillerino.ppaddict.chat.irc.EmbeddedIrcServerRule;
import org.tillerino.ppaddict.chat.local.LocalGameChatEventQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatResponseQueue;

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

	@Override
	protected String getWsUrl(Injector injector) throws DeploymentException {
		return "ws://" + getLive().getContainerIpAddress() + ":" + getLive().getMappedPort(8080) + "/live/v0";
	}

	@Override
	Injector createInjector() {
		return Guice.createInjector(new FullBotConfiguration(ircHost(), ircPort()));
	}

	@Override
	protected void startMore(Injector injector) {
		started.add(exec.submit(injector.getInstance(LocalGameChatEventQueue.class)));
		started.add(exec.submit(injector.getInstance(LocalGameChatResponseQueue.class)));
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

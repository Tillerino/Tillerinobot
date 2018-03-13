package tillerino.tillerinobot;

import static org.junit.Assert.assertFalse;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.junit.rules.ExternalResource;
import org.tillerino.irc.server.ConnectionInitiator;

/**
 * Wraps a TwIRC instance in an {@link ExternalResource}. The server runs in a
 * separate thread and binds to an open port which, can be accessed through
 * {@link #getPort()}. After the unit test is done, the server is shut. If the
 * thread doesn't terminate after ten seconds an {@link AssertionError} is
 * thrown.
 */
public class EmbeddedIrcServerRule extends ExternalResource {
	private Thread thread;

	private ServerSocketChannel socket;

	@Override
	protected void before() throws Throwable {
		socket = ServerSocketChannel.open().bind(new InetSocketAddress("localhost", 0));
		ConnectionInitiator initiator = new ConnectionInitiator(socket);
		thread = new Thread(initiator);
		thread.start();
	}

	public int getPort() {
		return socket.socket().getLocalPort();
	}

	@Override
	protected void after() {
		thread.interrupt();
		try {
			thread.join(10000);
		} catch (InterruptedException e) {
			// ignore in unit test
		}
		assertFalse("IRC server quit", thread.isAlive());
	}
}

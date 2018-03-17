package tillerino.tillerinobot.rest;

import java.net.URI;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.rules.ExternalResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JettyServerResource extends ExternalResource {
	private final Application app;

	private final String host;

	private final int port;

	private int actualPort = 0;

	private Server server;

	@Override
	protected void before() throws Throwable {
		server = JettyHttpContainerFactory.createServer(new URI("http", null, host, port, null, null, null),
				ResourceConfig.forApplication(app));
		actualPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		server.start();
	}

	@Override
	protected void after() {
		try {
			server.stop();
		} catch (Exception e) {
			log.error("Stopping Jetty failed", e);
		}
	}

	public int getPort() {
		return actualPort;
	}
}

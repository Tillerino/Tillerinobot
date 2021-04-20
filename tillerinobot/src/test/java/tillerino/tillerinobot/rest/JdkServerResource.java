package tillerino.tillerinobot.rest;

import java.net.URI;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.rules.ExternalResource;

import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JdkServerResource extends ExternalResource {
	private final Application app;

	private final String host;

	private final int port;

	private int actualPort = 0;

	private HttpServer server;

	@Override
	protected void before() throws Throwable {
		server = JdkHttpServerFactory.createHttpServer(new URI("http", null, host, port, "/", null, null),
				ResourceConfig.forApplication(app));
		actualPort = server.getAddress().getPort();
	}

	@Override
	protected void after() {
		try {
			server.stop(1);
		} catch (Exception e) {
			log.error("Stopping Jetty failed", e);
		}
	}

	public int getPort() {
		return actualPort;
	}
}

package tillerino.tillerinobot.websocket;

import java.util.Optional;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.rules.ExternalResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JettyWebsocketServerResource extends ExternalResource {
	private final String host;

	private final int port;

	private int actualPort = 0;

	private Server server;

	private ServerContainer wscontainer;

	@Override
	protected void before() throws Exception {
		start();
	}

	@Override
	protected void after() {
		try {
			stop();
		} catch (Exception e) {
			log.error("Stopping Jetty failed", e);
		}
	}

	public void start() throws Exception {
		server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(host);
		connector.setPort(port);
		server.addConnector(connector);
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		wscontainer = WebSocketServerContainerInitializer.configureContext(context);
		
		server.start();
		actualPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		log.info("Started Jetty Websocket Server on http://{}:{}/", host, actualPort);
	}

	public void stop() throws Exception {
		server.stop();
	}

	public int getPort() {
		return actualPort;
	}

	public void addEndpoint(Object endpoint) throws DeploymentException {
		String path = Optional.ofNullable(endpoint.getClass().getAnnotation(ServerEndpoint.class))
				.map(ServerEndpoint::value).orElseThrow(() -> new RuntimeException(endpoint.getClass().toString()));
		ServerEndpointConfig endpointConfig = ServerEndpointConfig.Builder.create(endpoint.getClass(), path).configurator(new Configurator() {
			@Override
			public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
				return (T) endpoint;
			}
		}).build();
		wscontainer.addEndpoint(endpointConfig);
	}

	public static void main(String[] args) throws Exception {
		JettyWebsocketServerResource resource = new JettyWebsocketServerResource("localhost", 8080);
		resource.before();
		resource.addEndpoint(new LiveActivityEndpoint());
		resource.server.join();
	}
}

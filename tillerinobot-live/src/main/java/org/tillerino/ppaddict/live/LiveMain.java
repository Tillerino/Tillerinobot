package org.tillerino.ppaddict.live;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.ppaddict.rabbit.AbstractRabbitMain;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiveMain extends AbstractRabbitMain {
	final LiveActivityEndpoint live;
	private final Undertow undertow;
	private final XnioWorker xnioWorker;

	public LiveMain(int port, String rabbitHost, int rabbitPort) throws ServletException, IOException, TimeoutException {
		super(log, rabbitHost, rabbitPort);
		live = new LiveActivityEndpoint();
		Xnio xnio = Xnio.getInstance("nio", Undertow.class.getClassLoader());
		xnioWorker = xnio.createWorker(OptionMap.builder().getMap());
		undertow = undertow(live, port);
	}

	private Undertow undertow(LiveActivityEndpoint live, int port) throws ServletException, IOException {
		final WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo()
				.addEndpoint(buildServerEndpointConfig(live))
				.setWorker(xnioWorker);
		final DeploymentManager deployment = defaultContainer()
				.addDeployment(deployment()
						.setClassLoader(LiveMain.class.getClassLoader())
						.setContextPath("/")
						.setDeploymentName("embedded-websockets")
						.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets)
						.addServlet(new ServletInfo("health", HealthServlet.class, () ->
								new ImmediateInstanceHandle<>(new HealthServlet(this::getRabbitChannel)))
								.addMappings("/live", "/ready")));

		deployment.deploy();
		return Undertow.builder().addHttpListener(port, "0.0.0.0")
				.setHandler(deployment.start())
				.build();
	}

	private static ServerEndpointConfig buildServerEndpointConfig(LiveActivityEndpoint endpoint) {
		String path = Optional.ofNullable(endpoint.getClass().getAnnotation(ServerEndpoint.class))
				.map(ServerEndpoint::value).orElseThrow(() -> new RuntimeException(endpoint.getClass().toString()));
		return ServerEndpointConfig.Builder.create(endpoint.getClass(), path)
				.configurator(new Configurator() {
					@Override
					public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
						return (T) endpoint;
					}
				}).build();
	}

	public void start(String connectionName) throws IOException, TimeoutException {
		log.info("Starting Undertow");
		undertow.start();
		log.info("Undertow started");
		try {
			super.start(connectionName);
			RemoteLiveActivity queue = RabbitMqConfiguration.liveActivity(getRabbitChannel());
			queue.setup();
			queue.subscribe(message -> message.visit(live));
		} catch (IOException | TimeoutException e) {
			stop();
			throw e;
		}
	}

	public void stop() throws IOException, TimeoutException {
		super.stop();
		undertow.stop();
		xnioWorker.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new LiveMain(env("PORT").map(parse("PORT")).orElse(8080),
				env("RABBIT_HOST").orElse("rabbitmq"),
				env("RABBIT_PORT").map(parse("RABBIT_PORT")).orElse(5672))
				.start("tillerinobot-live");
	}

	private static Optional<String> env(String name) {
		return Optional.ofNullable(System.getenv(name)).filter(StringUtils::isNotBlank);
	}

	private static Function<String, Integer> parse(String name) {
		return s -> {
			try {
				return Integer.valueOf(s);
			} catch (NumberFormatException e) {
				throw new NumberFormatException(name + ": " + e.getMessage());
			}
		};
	}
}

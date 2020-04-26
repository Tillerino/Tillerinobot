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
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

public class LiveMain {
	private final ConnectionFactory rabbitFactory;
	final LiveActivityEndpoint live;
	private final Undertow undertow;
	private final XnioWorker xnioWorker;

	private Connection rabbitConnection;
	private Channel rabbitChannel;

	public LiveMain(int port, String rabbitHost, int rabbitPort) throws ServletException, IOException, TimeoutException {
		live = new LiveActivityEndpoint();
		Xnio xnio = Xnio.getInstance("nio", Undertow.class.getClassLoader());
		xnioWorker = xnio.createWorker(OptionMap.builder().getMap());
		undertow = undertow(live, port);
		rabbitFactory = RabbitMqConfiguration.connectionFactory(rabbitHost, rabbitPort);
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
								new ImmediateInstanceHandle<>(new HealthServlet(() -> rabbitChannel)))
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

	public void start() throws IOException, TimeoutException {
		undertow.start();
		try {
			rabbitConnection = rabbitFactory.newConnection();
			rabbitChannel = rabbitConnection.createChannel();
			RemoteLiveActivity queue = RabbitMqConfiguration.liveActivity(rabbitChannel);
			queue.setup();
			queue.subscribe(message -> message.visit(live));
		} catch (IOException | TimeoutException e) {
			stop();
			throw e;
		}
	}

	public void stop() throws IOException, TimeoutException {
		if (rabbitChannel != null) {
			rabbitChannel.close();
		}
		if (rabbitConnection != null) {
			rabbitConnection.close();
		}
		undertow.stop();
		xnioWorker.shutdown();
	}

	public static void main(String[] args) throws Exception {
		new LiveMain(env("PORT").map(parse("PORT")).orElse(8080),
				env("RABBIT_HOST").orElse("rabbitmq"),
				env("RABBIT_PORT").map(parse("RABBIT_PORT")).orElse(5672))
				.start();
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

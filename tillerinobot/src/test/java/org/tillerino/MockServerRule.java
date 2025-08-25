package org.tillerino;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;
import org.tillerino.ppaddict.util.DockerNetwork;

import javax.inject.Named;

import lombok.SneakyThrows;

/**
 * Shorthands for using a mock server in tests.
 * The mock server is not stopped by the rule, but kept alive until the VM exits.
 * All assertions are reset after each test.
 *
 * Use with MockServerModule to inject mocked URLs.
 */
public class MockServerRule implements BeforeEachCallback, AfterEachCallback {
	private static final DockerImageName IMAGE = DockerImageName.parse("mockserver/mockserver").withTag(getMockServerVersion());
	private static final MockServerContainer MOCK_SERVER = new MockServerContainer(IMAGE)
			.withNetwork(DockerNetwork.NETWORK)
			.withNetworkAliases("mockserver");
	private static final MockServerClient CLIENT;

	static {
		MOCK_SERVER.start();
		CLIENT = new MockServerClient(MOCK_SERVER.getContainerIpAddress(), MOCK_SERVER.getMappedPort(1080));
	}

	@SneakyThrows
	private static String getMockServerVersion() {
		try (InputStream is = MockServerRule.class.getResourceAsStream("/META-INF/maven/org.mock-server/mockserver-client-java/pom.properties")) {
			Properties props = new Properties();
			props.load(is);
			return (String) props.get("version");
		}
	}

	public static String getExternalMockServerAddress() {
		return "http://" + MOCK_SERVER.getContainerIpAddress() + ":" + MOCK_SERVER.getMappedPort(1080);
	}

	public void mockJsonGet(String relativePath, String jsonResponse, String... headers) {
		CLIENT.when(getRequest(relativePath, headers))
			.respond(HttpResponse.response()
					// for some reason JsonBody doesn't set the content-type?
					.withBody(jsonResponse)
					.withHeader("content-type", "application/json"));
	}

	private HttpRequest getRequest(String relativePath, String... headers) {
		HttpRequest request = HttpRequest.request(relativePath).withMethod("GET");
		assertThat(headers.length % 2).isZero();
		for (int i = 0; i < headers.length; i+=2) {
			request = request.withHeader(headers[i], headers[i + 1]);
		}
		return request;
	}

	public void mockStatusCodeGet(String relativePath, int code, String... headers) {
		CLIENT.when(getRequest(relativePath))
			.respond(HttpResponse.response().withStatusCode(code));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		CLIENT.reset();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if(context.getExecutionException().isPresent()) {
		Stream.of(CLIENT.retrieveLogMessagesArray(null))
			.filter(x -> x.contains("no expectation for"))
			.forEach(System.err::println);
		}
	}

	/**
	 * This injects URLs for services which we mock via the mockserver.
	 */
	@dagger.Module
	public interface MockServerModule {
		@dagger.Provides
		@Named("ppaddict.auth.url") static String u() {
			return getExternalMockServerAddress() + "/auth";
		}
	}

	public static MockServerClient mockServer() {
		return CLIENT;
	}
}

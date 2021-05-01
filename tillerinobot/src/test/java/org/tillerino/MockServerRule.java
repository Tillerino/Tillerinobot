package org.tillerino;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;
import org.tillerino.ppaddict.util.DockerNetwork;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Shorthands for using a mock server in tests.
 * The mock server is not stopped by the rule, but kept alive until the VM exits.
 * All assertions are reset after each test.
 *
 * Use with MockServerModule to inject mocked URLs.
 */
public class MockServerRule extends TestWatcher {
	private static final DockerImageName IMAGE = DockerImageName.parse("jamesdbloom/mockserver").withTag("mockserver-5.11.2");
	private static final MockServerContainer MOCK_SERVER = new MockServerContainer(IMAGE)
			.withNetwork(DockerNetwork.NETWORK)
			.withNetworkAliases("mockserver");
	private static final MockServerClient CLIENT;

	static {
		MOCK_SERVER.start();
		CLIENT = new MockServerClient(MOCK_SERVER.getContainerIpAddress(), MOCK_SERVER.getMappedPort(1080));
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
		assertThat(headers.length % 2).isEqualTo(0);
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
	public Statement apply(Statement base, Description description) {
		return super.apply(base, description);
	}

	@Override
	protected void finished(Description description) {
		try {
			CLIENT.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This injects URLs for services which we mock via the mockserver.
	 */
	public static class MockServerModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(String.class).annotatedWith(Names.named("ppaddict.auth.url")).toInstance(getExternalMockServerAddress() + "/auth");
		}
	}

	public static MockServerClient mockServer() {
		return CLIENT;
	}
}

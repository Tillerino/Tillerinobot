package org.tillerino;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.NearMiss;

import java.util.List;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.tillerino.ppaddict.util.DockerNetwork;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import javax.inject.Named;

/**
 * Shorthands for using a mock server in tests.
 * The mock server is not stopped by the rule, but kept alive until the VM exits.
 * All assertions are reset after each test.
 *
 * Use with MockServerModule to inject mocked URLs.
 */
public class MockServerRule implements BeforeEachCallback, AfterEachCallback {
	private static final WireMockContainer MOCK_SERVER = new WireMockContainer(WireMockContainer.WIREMOCK_2_LATEST)
			.withNetwork(DockerNetwork.NETWORK)
			.withNetworkAliases("mockserver");
	private static final WireMock CLIENT;

	static {
		MOCK_SERVER.start();
		System.out.println(MOCK_SERVER.getMappedPort(8080));
		CLIENT = new WireMock(MOCK_SERVER.getHost(), MOCK_SERVER.getMappedPort(8080));
	}

	public static String getExternalMockServerAddress() {
		return "http://" + MOCK_SERVER.getContainerIpAddress() + ":" + MOCK_SERVER.getMappedPort(8080);
	}

	public static String getDockerMockServerAddress() {
		return "http://mockserver:8080";
	}

	public void mockJsonGet(String relativePath, String jsonResponseBody, String... headers) {
		mockJsonGetWithRequestBody(relativePath, null, jsonResponseBody, headers);
	}

	public void mockJsonGetWithRequestBody(String relativePath, String jsonRequestBody, String jsonResponseBody,
			String... headers) {
		MappingBuilder request = get(urlEqualTo(relativePath));
		if (jsonRequestBody != null) {
			request.withRequestBody(equalToJson(jsonRequestBody));
		}
		assertThat(headers.length % 2).isZero();
		for (int i = 0; i < headers.length; i+=2) {
			request = request.withHeader(headers[i], WireMock.equalTo(headers[i + 1]));
		}
		mockServer().register(request
			.willReturn(aResponse()
					.withHeader("Content-Type", "application/json;charset=UTF-8")
					.withBody(jsonResponseBody)));
	}

	public void mockStatusCodeGet(String relativePath, int code, String... headers) {
		MappingBuilder request = get(urlEqualTo(relativePath));
		assertThat(headers.length % 2).isZero();
		for (int i = 0; i < headers.length; i+=2) {
			request = request.withHeader(headers[i], WireMock.equalTo(headers[i + 1]));
		}
		mockServer().register(request
			.willReturn(aResponse().withStatus(code)));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		CLIENT.resetRequests();
		CLIENT.resetToDefaultMappings();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if(context.getExecutionException().isPresent()) {
			List<NearMiss> nearMisses = CLIENT.findNearMissesForAllUnmatchedRequests();
			if (!nearMisses.isEmpty()) {
				nearMisses.forEach(System.err::println);
			}
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

	public static WireMock mockServer() {
		return CLIENT;
	}
}

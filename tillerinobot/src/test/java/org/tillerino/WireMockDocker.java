package org.tillerino;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.omkelderman.sandoku.ProcessorApi;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import dagger.Provides;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;
import org.tillerino.ppaddict.util.DockerNetwork;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

/**
 * Shorthands for using a mock server in tests. The mock server is not stopped by the rule, but kept alive until the VM
 * exits. All assertions are reset after each test.
 *
 * <p>Use with MockServerModule to inject mocked URLs.
 */
public class WireMockDocker implements BeforeEachCallback, AfterEachCallback {
    private static final WireMockContainer WIRE_MOCK = new WireMockContainer(WireMockContainer.WIREMOCK_2_LATEST)
            .withNetwork(DockerNetwork.NETWORK)
            .withNetworkAliases("mockserver")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(cmd.getHostConfig()
                    .withMounts(List.of(
                            new Mount()
                                    .withSource(Paths.get("../../Tillerinobot/tillerinobot/src/test/wiremock/mappings")
                                            .normalize()
                                            .toAbsolutePath()
                                            .toString())
                                    .withTarget("/home/wiremock/mappings")
                                    .withType(MountType.BIND),
                            new Mount()
                                    .withSource(Paths.get("../../Tillerinobot/tillerinobot/src/test/wiremock/__files")
                                            .normalize()
                                            .toAbsolutePath()
                                            .toString())
                                    .withTarget("/home/wiremock/__files")
                                    .withType(MountType.BIND)))));
    private static final WireMock CLIENT;

    static {
        WIRE_MOCK.start();
        System.out.println(WIRE_MOCK.getMappedPort(8080));
        CLIENT = new WireMock(WIRE_MOCK.getHost(), WIRE_MOCK.getMappedPort(8080));
    }

    public static String getExternalAddress() {
        return "http://" + WIRE_MOCK.getHost() + ":" + WIRE_MOCK.getMappedPort(8080);
    }

    public static String getDockerAddress() {
        return "http://mockserver:8080";
    }

    public void mockJsonGet(String relativePath, String jsonResponseBody, String... headers) {
        mockJsonGetWithRequestBody(relativePath, null, jsonResponseBody, headers);
    }

    public void mockJsonGetWithRequestBody(
            String relativePath, String jsonRequestBody, String jsonResponseBody, String... headers) {
        MappingBuilder request = get(urlEqualTo(relativePath));
        if (jsonRequestBody != null) {
            request.withRequestBody(equalToJson(jsonRequestBody));
        }
        assertThat(headers.length % 2).isZero();
        for (int i = 0; i < headers.length; i += 2) {
            request = request.withHeader(headers[i], WireMock.equalTo(headers[i + 1]));
        }
        mockServer()
                .register(request.willReturn(aResponse()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(jsonResponseBody)));
    }

    public void mockStatusCodeGet(String relativePath, int code, String... headers) {
        MappingBuilder request = get(urlEqualTo(relativePath));
        assertThat(headers.length % 2).isZero();
        for (int i = 0; i < headers.length; i += 2) {
            request = request.withHeader(headers[i], WireMock.equalTo(headers[i + 1]));
        }
        mockServer().register(request.willReturn(aResponse().withStatus(code)));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        CLIENT.resetRequests();
        CLIENT.resetToDefaultMappings();
        registerOsuApiV1MissingResponses();
    }

    /** osu! API v1 will not return 404s, but instead empty arrays or something. */
    private static void registerOsuApiV1MissingResponses() {
        CLIENT.register(get(urlPathMatching("/api/get_beatmaps.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("[]"))
                .atPriority(Integer.MAX_VALUE));

        CLIENT.register(get(urlPathMatching("/api/get_user.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody("[]"))
                .atPriority(Integer.MAX_VALUE));

        // did not bother to mock the rest. add more, when you need to.
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            List<NearMiss> nearMisses = CLIENT.findNearMissesForAllUnmatchedRequests();
            if (!nearMisses.isEmpty()) {
                nearMisses.forEach(System.err::println);
            }
        }
    }

    /** This injects URLs for services which we mock via the mockserver. */
    @dagger.Module
    public interface Module {
        @dagger.Provides
        @Named("ppaddict.auth.url")
        static String u() {
            return getExternalAddress() + "/auth";
        }

        @dagger.Provides
        @Named("nap.url")
        static URI napUrl() {
            return URI.create(getExternalAddress() + "/nap");
        }

        @dagger.Provides
        @Named("nap.token")
        static String napToken() {
            return "nap-token-fake";
        }

        @Provides
        @Singleton
        static ProcessorApi sanDoku() {
            return spy(SanDoku.defaultClient(URI.create("http://localhost:8080")));
        }

        @Provides
        @Singleton
        @SneakyThrows
        static OsuApiV1 osuApiV1() {
            return spy(new OsuApiV1(
                    URI.create(WireMockDocker.getExternalAddress() + "/api/").toURL(),
                    OsuApiV1Test.OSUAPI_V1_MOCK_KEY,
                    RateLimiter.unlimited()));
        }

        @Provides
        @Singleton
        static OsuApiV2 osuApiV2() {
            URI baseUrl = URI.create(WireMockDocker.getExternalAddress());
            return spy(new OsuApiV2(
                    baseUrl,
                    TokenCache.inMemory(baseUrl, OsuApiV2Test.OSUAPI_V2_MOCK_CREDENTIALS),
                    RateLimiter.unlimited()));
        }

        @Provides
        @Singleton
        static BeatmapDownloader l() {
            BeatmapDownloader wireMockBeatmapDownloader = WebResourceFactory.newResource(
                    BeatmapDownloader.class,
                    JerseyClientBuilder.createClient().target(WireMockDocker.getExternalAddress()));
            return Mockito.spy(wireMockBeatmapDownloader);
        }
    }

    public static WireMock mockServer() {
        return CLIENT;
    }
}

package org.tillerino.ppaddict.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.tillerino.ppaddict.live.LiveContainer.getLive;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * The plain HTTP endpoints. The websocket endpoint is covered by {@link LiveActivityIT} and {@link MemoryIT}.
 *
 * <p>The "not ready" branch of {@code /ready} isn't covered here: it needs RabbitMQ to be down while the container is
 * running, which the shared container doesn't allow. The irc module has the same problem and disabled that test.
 */
public class HealthIT {
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    public void liveness() throws Exception {
        HttpResponse<String> response = get("/live");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("live\n");
    }

    @Test
    public void readiness() {
        // LiveContainer waits for this before starting, so it should be ready right away. Still, be lenient.
        await().untilAsserted(() -> {
            HttpResponse<String> response = get("/ready");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("ready\n");
        });
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(new URI(
                        "http://" + getLive().getHost() + ":" + getLive().getMappedPort(8080) + path))
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

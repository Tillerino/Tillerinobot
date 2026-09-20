package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class DefaultGuiTest extends AbstractPlaywrightTest {
    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + getPort() + path))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void rootServesV2Html() throws Exception {
        HttpResponse<String> response = get("/");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("switch to old interface");
    }

    @Test
    void rootKeepsDeepLinkQuery() {
        Response initial = page.waitForResponse(
                "**/htmx/beatmaps**", () -> page.navigate("http://localhost:" + getPort() + "/?b=1234"));
        assertThat(initial.url()).contains("?b=1234");
        assertThat(page.url()).doesNotContain("v2.html");
    }

    @Test
    void redirectsPreserveQuery() throws Exception {
        assertThat(get("/Ppaddict.html?b=1234").headers().firstValue("Location"))
                .contains("/v1.html?b=1234");
        assertThat(get("/Ppaddict.html").headers().firstValue("Location")).contains("/v1.html");
    }

    @Test
    void rootLandsOnV2Gui() {
        page.navigate("http://localhost:" + getPort() + "/");
        page.waitForResponse("**/htmx/user**", () -> {});
        page.waitForResponse("**/htmx/beatmaps**", () -> {});

        PlaywrightAssertions.assertThat(page.locator(".header-bar h1")).isVisible();
        assertThat(page.url()).endsWith("/");
    }

    @Test
    void guiSwitchLinks() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForResponse("**/htmx/user**", () -> {});
        PlaywrightAssertions.assertThat(page.locator("#user-area a[href='v1.html']"))
                .isVisible();
    }
}

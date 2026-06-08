package org.tillerino.ppaddict;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class LoginTest extends AbstractPlaywrightTest {

    @Test
    void loginLogoutFlow() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForResponse("**/htmx/user**", () -> {});
        page.waitForResponse("**/htmx/beatmaps**", () -> {});

        PlaywrightAssertions.assertThat(page.locator("#user-area button:has-text('Login')"))
                .isVisible();

        page.locator("#user-area button:has-text('Login')").click();

        assertThat(page.locator("#login-modal").getAttribute("open")).isNotNull();

        page.locator("#login-modal a").click();
        page.waitForLoadState();

        assertThat(page.url()).contains(org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite.PATH);

        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        PlaywrightAssertions.assertThat(page.locator("#user-area .username")).isVisible();
        assertThat(page.locator("#user-area .username").textContent()).isEqualTo("TestUser");

        page.locator("#user-area a[href*='authlogout']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area button:has-text('Login')");

        PlaywrightAssertions.assertThat(page.locator("#user-area button:has-text('Login')"))
                .isVisible();
    }
}

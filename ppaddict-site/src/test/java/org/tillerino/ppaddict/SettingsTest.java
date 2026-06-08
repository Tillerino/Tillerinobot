package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class SettingsTest extends AbstractPlaywrightTest {

    @Test
    void saveAndResetSettings() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForResponse("**/htmx/user**", () -> {});
        page.waitForResponse("**/htmx/beatmaps**", () -> {});

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();

        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='openDirectOnMapSelect']");
        PlaywrightAssertions.assertThat(page.locator("#settings-modal")).isVisible();
        assertThat(page.locator("#settings-modal input[name='recommendationsParameters']")
                        .inputValue())
                .isEqualTo("*");

        page.locator("#settings-modal input[name='lowAccuracy']").fill("95");
        page.locator("#settings-modal button:has-text('Save')").click();
        page.waitForTimeout(2000);
        PlaywrightAssertions.assertThat(page.locator("#settings-save-result")).hasText("Saved.");
        page.locator("#settings-modal").evaluate("el => el.close()");

        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='lowAccuracy']");
        assertThat(page.locator("#settings-modal input[name='lowAccuracy']").inputValue())
                .isEqualTo("95");
        page.locator("#settings-modal input[name='lowAccuracy']").fill("80");
        page.locator("#settings-modal input[name='highAccuracy']").fill("90");
        page.locator("#settings-modal button:has-text('Save')").click();
        page.waitForTimeout(2000);
        PlaywrightAssertions.assertThat(page.locator("#settings-save-result")).hasText("Saved.");
        page.locator("#settings-modal").evaluate("el => el.close()");

        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='lowAccuracy']");
        assertThat(page.locator("#settings-modal input[name='lowAccuracy']").inputValue())
                .isEqualTo("80");
        assertThat(page.locator("#settings-modal input[name='highAccuracy']").inputValue())
                .isEqualTo("90");
        page.locator("#settings-modal").evaluate("el => el.close()");

        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='lowAccuracy']");
        page.locator("#settings-modal input[name='lowAccuracy']").fill("");
        page.locator("#settings-modal input[name='highAccuracy']").fill("");
        page.locator("#settings-modal button:has-text('Save')").click();
        page.waitForTimeout(2000);
        PlaywrightAssertions.assertThat(page.locator("#settings-save-result")).hasText("Saved.");
        page.locator("#settings-modal").evaluate("el => el.close()");

        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='lowAccuracy']");
        assertThat(page.locator("#settings-modal input[name='lowAccuracy']").inputValue())
                .isEqualTo("93");
        assertThat(page.locator("#settings-modal input[name='highAccuracy']").inputValue())
                .isEqualTo("100");
        page.locator("#settings-modal").evaluate("el => el.close()");
    }
}

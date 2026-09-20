package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class SettingsTest extends AbstractPlaywrightTest {

    private void loginAndLink() throws Exception {
        // register the response waiters before navigating, or a fast response may race the registration
        page.waitForResponse(
                "**/htmx/user**",
                () -> page.waitForResponse(
                        "**/htmx/beatmaps**", () -> page.navigate("http://localhost:" + getPort() + "/v2.html")));

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();

        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        String token = userDataService.getLinkString("local:TestUser", "TestUser");
        userDataService.tryLinkToPpaddict(token, 12345);
    }

    @Test
    void settingsSaveAndClear() throws Exception {
        loginAndLink();

        // Default values
        openSettings();
        PlaywrightAssertions.assertThat(page.locator("#settings-modal")).isVisible();
        assertSetting("recommendationsParameters", "*");
        assertSetting("lowAccuracy", "93");
        assertSetting("highAccuracy", "100");
        closeSettings();

        // Set custom values
        openSettings();
        setSetting("lowAccuracy", "80");
        setSetting("highAccuracy", "90");
        setSetting("recommendationsParameters", "gamma AR=9");
        saveSettings();
        closeSettings();

        openSettings();
        assertSetting("lowAccuracy", "80");
        assertSetting("highAccuracy", "90");
        assertSetting("recommendationsParameters", "gamma AR=9");
        closeSettings();

        // Clear values
        openSettings();
        setSetting("lowAccuracy", "");
        setSetting("highAccuracy", "");
        setSetting("recommendationsParameters", "");
        saveSettings();
        closeSettings();

        openSettings();
        assertSetting("lowAccuracy", "93");
        assertSetting("highAccuracy", "100");
        assertSetting("recommendationsParameters", "*");
        closeSettings();
    }

    @Test
    void apiKeyCreation() throws Exception {
        loginAndLink();

        openSettings();
        page.locator("#settings-modal button:has-text('Create API key')").click();
        page.waitForSelector("#apikey-confirm-modal");
        PlaywrightAssertions.assertThat(page.locator("#apikey-confirm-modal")).isVisible();

        page.locator("#apikey-confirm-modal button:has-text('Confirm')").click();
        page.waitForSelector("#apikey-confirm-modal code");
        PlaywrightAssertions.assertThat(page.locator("#apikey-confirm-modal code"))
                .isVisible();

        String apiKey = page.locator("#apikey-confirm-modal code").textContent();
        assertThat(apiKey).isNotNull();
        assertThat(apiKey).isNotEmpty();
        assertThat(apiKey).hasSize(40);
    }

    private void openSettings() {
        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='openDirectOnMapSelect']");
    }

    private void closeSettings() {
        page.locator("#settings-modal").evaluate("el => el.close()");
    }

    private void saveSettings() {
        page.locator("#settings-modal button:has-text('Save')").click();
        PlaywrightAssertions.assertThat(page.locator("#settings-save-result")).hasText("Reloading in 5s...");
    }

    private void setSetting(String name, String value) {
        page.locator("#settings-modal input[name='" + name + "']").fill(value);
    }

    private String getSetting(String name) {
        return page.locator("#settings-modal input[name='" + name + "']").inputValue();
    }

    private void assertSetting(String name, String expected) {
        assertThat(getSetting(name)).isEqualTo(expected);
    }
}

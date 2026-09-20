package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class WelcomeAndHelpTest extends AbstractPlaywrightTest {

    @Test
    void welcomeDialogShownOnce() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForLoadState();

        assertThat(page.locator("#welcome-dialog").getAttribute("open")).isNotNull();
        assertThat(page.locator("#welcome-dialog").textContent()).contains("Welcome to ppaddict");

        page.reload();
        page.waitForLoadState();

        assertThat(page.locator("#welcome-dialog").getAttribute("open")).isNull();
    }

    @Test
    void helpPopupsShowAndClose() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForLoadState();
        page.waitForSelector("table.beatmaps");

        // filter row is hidden by default
        assertThat(filterRowDisplay()).isEqualTo("none");

        page.locator(".header-bar a:has-text('Help')").click();

        // popups appear, including the filter help popups (filters auto-opened)
        assertThat(page.locator(".helppopup").count()).isEqualTo(8);
        PlaywrightAssertions.assertThat(page.locator(".helppopup:has-text('pp values')"))
                .isVisible();
        PlaywrightAssertions.assertThat(page.locator(".helppopup:has-text('meta information')"))
                .isVisible();
        assertThat(filterRowDisplay()).isEqualTo("table-row");
        PlaywrightAssertions.assertThat(page.locator(".help-backdrop")).isVisible();

        // clicking the backdrop closes the help and re-hides the filter row
        page.mouse().click(850, 60);
        assertThat(page.locator(".helppopup").count()).isEqualTo(0);
        assertThat(page.locator(".help-backdrop").count()).isEqualTo(0);
        assertThat(filterRowDisplay()).isEqualTo("none");
    }

    @Test
    void helpPopupsOnRecommendationsPage() {
        page.navigate("http://localhost:" + getPort() + "/r.html");
        page.waitForLoadState();

        // login (auto-links via fake auth) so that the recommendations table renders
        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        page.reload();
        page.waitForLoadState();
        page.waitForSelector("table.beatmaps");

        page.locator(".header-bar a:has-text('Help')").click();

        // the recommendations help shows instead of the r! button help on the recommendations page
        PlaywrightAssertions.assertThat(page.locator(".helppopup:has-text('personal recommendations')"))
                .isVisible();
        assertThat(page.locator(".helppopup:has-text('Click r! to access')").count())
                .isEqualTo(0);
    }

    @Test
    void rowClickOpensOsuDirectWhenEnabled() throws Exception {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForLoadState();

        // login (auto-links via fake auth)
        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        page.reload();
        page.waitForLoadState();
        page.waitForSelector("table.beatmaps");

        // default: row click does not open osu!direct
        assertThat(page.locator("table.beatmaps tbody tr").first().getAttribute("onclick"))
                .isNull();

        // enable the setting
        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='openDirectOnMapSelect']");
        page.locator("#settings-modal input[name='openDirectOnMapSelect']").check();
        page.locator("#settings-modal button:has-text('Save')").click();
        PlaywrightAssertions.assertThat(page.locator("#settings-save-result")).hasText("Reloading in 5s...");

        page.reload();
        page.waitForLoadState();
        page.waitForSelector("table.beatmaps");

        var onclick = page.locator("table.beatmaps tbody tr").first().getAttribute("onclick");
        assertThat(onclick).contains("osu://b/");
        assertThat(onclick).contains("closest('a,button,input')");
    }

    @Test
    void recommendationsSettingsHelp() throws Exception {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForLoadState();

        // login (auto-links via fake auth)
        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // open the settings dialog and show help via the link inside the dialog:
        // only the settings help appears (v1 stack behavior)
        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='recommendationsParameters']");
        page.locator("#settings-modal a:has-text('Help')").click();

        PlaywrightAssertions.assertThat(page.locator(".helppopup:has-text('Recommendations settings')"))
                .isVisible();
        PlaywrightAssertions.assertThat(page.locator(".help-backdrop")).isVisible();
        assertThat(page.locator(".helppopup:has-text('pp values')").count()).isEqualTo(0);

        // dismissing the help keeps the settings dialog open
        page.mouse().click(400, 600);
        assertThat(page.locator(".helppopup").count()).isEqualTo(0);
        assertThat(page.locator(".help-backdrop").count()).isEqualTo(0);
        assertThat(page.locator("#settings-modal").getAttribute("open")).isNotNull();
    }

    @Test
    void settingsDialogDismissal() throws Exception {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForLoadState();

        // login (auto-links via fake auth)
        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // open the settings dialog
        page.locator("#user-area button:has-text('Settings')").click();
        page.waitForSelector("#settings-modal input[name='recommendationsParameters']");
        assertThat(page.locator("#settings-modal").getAttribute("open")).isNotNull();

        // clicking inside the dialog does not dismiss it
        page.locator("#settings-modal b:has-text('Settings')").click();
        assertThat(page.locator("#settings-modal").getAttribute("open")).isNotNull();

        // clicking outside (on the backdrop) dismisses it
        page.mouse().click(300, 600);
        assertThat(page.locator("#settings-modal").getAttribute("open")).isNull();
    }

    private String filterRowDisplay() {
        return (String) page.locator(".filter-row").evaluate("el => getComputedStyle(el).display");
    }
}

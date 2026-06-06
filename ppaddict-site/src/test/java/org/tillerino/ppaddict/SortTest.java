package org.tillerino.ppaddict;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class SortTest extends AbstractPlaywrightTest {

    @Test
    void sortToggle() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        // Initially no sort arrow on BPM
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("BPM");

        // Click to sort
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("↓BPM");

        // Click again to sort descending
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("↑BPM");

        // Click again to turn off sorting
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("BPM");
    }
}

package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class SortTest extends AbstractPlaywrightTest {

    @Test
    void sortToggle() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        // Initially no sort arrow on BPM
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("BPM");

        // Click to sort ascending
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("↓BPM");

        // Verify ascending BPM order
        assertThat(page.locator("tbody tr td:nth-of-type(11)").allTextContents())
                .extracting(Double::parseDouble)
                .isSorted();

        // Click again to sort descending
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("↑BPM");

        // Verify descending BPM order
        assertThat(page.locator("tbody tr td:nth-of-type(11)").allTextContents())
                .extracting(Double::parseDouble)
                .isSortedAccordingTo(Comparator.<Double>naturalOrder().reversed());

        // Click again to turn off sorting
        page.getByText("BPM").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});
        PlaywrightAssertions.assertThat(page.getByText("BPM").locator("xpath=.."))
                .hasText("BPM");
    }
}

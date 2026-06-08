package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class InfiniteScrollTest extends AbstractPlaywrightTest {

    @Test
    void infiniteScrollLoadsMoreBeatmaps() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        // Initial load: 100 beatmap rows + 1 sentinel
        var tbody = page.locator("tbody");
        PlaywrightAssertions.assertThat(tbody.locator("tr")).hasCount(101);

        // Sentinel is visible
        var sentinel = page.locator("td:has-text('Loading more...')");
        PlaywrightAssertions.assertThat(sentinel).isVisible();

        // Scroll sentinel into view to trigger intersection
        sentinel.scrollIntoViewIfNeeded();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        // After load: 200 beatmap rows + 1 new sentinel
        PlaywrightAssertions.assertThat(tbody.locator("tr")).hasCount(201);

        // Sentinel still exists for further scrolling
        PlaywrightAssertions.assertThat(page.locator("td:has-text('Loading more...')"))
                .isVisible();

        // Verify the new rows are different from the first batch
        var firstPageNames =
                page.locator("tbody tr").nth(0).locator("td:nth-of-type(4) a").textContent();
        var secondPageNames =
                page.locator("tbody tr").nth(100).locator("td:nth-of-type(4) a").textContent();
        assertThat(firstPageNames).isNotEqualTo(secondPageNames);
    }
}

package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class DeepLinksTest extends AbstractPlaywrightTest {

    @Test
    void setIdFilterShowsOnlyThatSet() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        Locator firstRow = page.locator("tbody tr[data-beatmapsetid]").first();
        String setId = firstRow.getAttribute("data-beatmapsetid");
        assertThat(setId).isNotNull();
        int setIdInt = Integer.parseInt(setId);

        // Page is not filtered
        assertThat(page.locator("tbody tr[data-beatmapsetid]").all())
                .hasSize(100)
                .extracting(row -> Integer.parseInt(row.getAttribute("data-beatmapsetid")))
                .anyMatch(i -> i != setIdInt);

        // Click more button on first row to get the set link
        firstRow.locator("td:nth-of-type(6) button").click();
        PlaywrightAssertions.assertThat(page.locator("dialog#more-dialog")).isVisible();

        // Navigate using the dialog link
        page.waitForResponse("**/htmx/beatmaps/initial**", () -> page.getByText("Show entire set")
                .click());
        PlaywrightAssertions.assertThat(page.locator("dialog#more-dialog"))
                .not()
                .isVisible();

        // Page is now filtered
        assertThat(page.locator("tbody tr[data-beatmapsetid]").all())
                .isNotEmpty()
                .hasSizeLessThan(100)
                .extracting(row -> Integer.parseInt(row.getAttribute("data-beatmapsetid")))
                .containsOnly(setIdInt);
    }

    @Test
    void beatmapIdFilterShowsOnlyThatBeatmap() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        Locator firstRow = page.locator("tbody tr[data-beatmapid]").first();
        String beatmapId = firstRow.getAttribute("data-beatmapid");
        assertThat(beatmapId).isNotNull();
        int beatmapIdInt = Integer.parseInt(beatmapId);

        // Page is not filtered
        assertThat(page.locator("tbody tr[data-beatmapid]").all())
                .hasSize(100)
                .extracting(row -> Integer.parseInt(row.getAttribute("data-beatmapid")))
                .anyMatch(i -> i != beatmapIdInt);

        // Click more button on first row to get the beatmap link
        firstRow.locator("td:nth-of-type(6) button").click();
        PlaywrightAssertions.assertThat(page.locator("dialog#more-dialog")).isVisible();

        // Navigate using the dialog link
        page.waitForResponse("**/htmx/beatmaps/initial**", () -> page.getByText("Show all mods")
                .click());
        PlaywrightAssertions.assertThat(page.locator("dialog#more-dialog"))
                .not()
                .isVisible();

        // Page is now filtered
        assertThat(page.locator("tbody tr[data-beatmapid]").all())
                .isNotEmpty()
                .hasSizeLessThan(100)
                .extracting(row -> Integer.parseInt(row.getAttribute("data-beatmapid")))
                .containsOnly(beatmapIdInt);
    }
}

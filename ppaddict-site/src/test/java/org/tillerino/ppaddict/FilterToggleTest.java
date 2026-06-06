package org.tillerino.ppaddict;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class FilterToggleTest extends AbstractPlaywrightTest {

    @Test
    void filterShowHideToggle() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isHidden();

        page.locator(".pager-row td:first-child button").click();

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isVisible();

        PlaywrightAssertions.assertThat(
                        page.locator(".filter-row input.chillinput").first())
                .isVisible();

        page.locator("tr.filter-row button").click();

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isHidden();
    }
}

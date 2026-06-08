package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class PaginationTest extends AbstractPlaywrightTest {

    private static final String PAGER_SELECTOR = "tr.pager-row td.pager";

    @Test
    void pagerRowExistsWithButtons() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        var pager = page.locator(PAGER_SELECTOR);
        PlaywrightAssertions.assertThat(pager).isVisible();

        var buttons = pager.locator("button");
        PlaywrightAssertions.assertThat(buttons).hasCount(5);
        PlaywrightAssertions.assertThat(buttons.nth(0)).hasText("⇤");
        PlaywrightAssertions.assertThat(buttons.nth(1)).hasText("←");
        PlaywrightAssertions.assertThat(buttons.nth(2)).hasText("→");
        PlaywrightAssertions.assertThat(buttons.nth(3)).hasText("↠");
        PlaywrightAssertions.assertThat(buttons.nth(4)).hasText("⇥");

        var counterText = pager.textContent();
        assertThat(counterText).contains("1-100 of");
    }

    @Test
    void firstPageDisabledButtons() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        var pager = page.locator(PAGER_SELECTOR);
        var buttons = pager.locator("button");

        PlaywrightAssertions.assertThat(buttons).hasCount(5);
        PlaywrightAssertions.assertThat(pager.locator("button:disabled")).hasCount(2);
        PlaywrightAssertions.assertThat(pager.locator("button:not([disabled])")).hasCount(3);
    }

    @Test
    void nextPageNavigation() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        var pager = page.locator(PAGER_SELECTOR);

        var namesBefore = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesBefore).hasSize(100);

        page.getByText("→").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesAfter = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfter).hasSize(100);
        assertThat(namesAfter).isNotEqualTo(namesBefore);

        var counterText = pager.textContent();
        assertThat(counterText).contains("101-200 of");
    }

    @Test
    void previousPageNavigation() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        var pager = page.locator(PAGER_SELECTOR);

        var namesBefore = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesBefore).hasSize(100);

        page.getByText("→").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesAfterNext = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfterNext).hasSize(100);
        assertThat(namesAfterNext).isNotEqualTo(namesBefore);

        page.getByText("←").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesAfterPrev = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfterPrev).hasSize(100);
        assertThat(namesAfterPrev).isEqualTo(namesBefore);

        var counterText = pager.textContent();
        assertThat(counterText).contains("1-100 of");
    }

    @Test
    void jumpForwardNavigation() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        var pager = page.locator(PAGER_SELECTOR);

        var namesBefore = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesBefore).hasSize(100);

        page.getByText("↠").click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesAfter = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfter).hasSize(100);
        assertThat(namesAfter).isNotEqualTo(namesBefore);

        var counterText = pager.textContent();
        assertThat(counterText).contains("1001-1100 of");
    }
}

package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.Test;

class RecommendationsTest extends AbstractPlaywrightTest {

    @Test
    void recommendationsPageFlow() throws Exception {
        page.navigate("http://localhost:" + getPort() + "/r.html");
        page.waitForLoadState();

        assertThat(page.locator("h1 a").allTextContents()).containsExactly("ppaddict", "!r");
        PlaywrightAssertions.assertThat(page.locator("#user-area button:has-text('Login')"))
                .isVisible();

        page.locator("#user-area button:has-text('Login')").click();
        assertThat(page.locator("#login-modal").getAttribute("open")).isNotNull();

        page.locator("#login-modal a").click();
        page.waitForLoadState();

        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        page.reload();
        page.waitForLoadState();
        page.waitForSelector(".table-container table");

        PlaywrightAssertions.assertThat(page.locator("#user-area .username")).isVisible();
        assertThat(page.locator("#user-area .username").textContent()).isEqualTo("TestUser");
        PlaywrightAssertions.assertThat(page.locator(".table-container table")).isVisible();

        var rows = page.locator(".table-container tbody tr").all();
        assertThat(rows).isNotEmpty();

        var hideButtons =
                page.locator(".table-container button:has-text('Hide')").all();
        assertThat(hideButtons).isNotEmpty();
        assertThat(hideButtons).hasSameSizeAs(rows);

        var namesBefore = page.locator(".table-container td:nth-of-type(4) a").allTextContents();
        assertThat(namesBefore).isNotEmpty();
        String firstBefore = namesBefore.get(0);

        // row buttons are hidden unless the row is hovered
        var firstRow = page.locator(".table-container tbody tr").first();
        page.waitForResponse("**/htmx/recommendations/hide**", () -> {
            firstRow.hover();
            firstRow.locator("button:has-text('Hide')").click();
        });

        var namesAfter = page.locator(".table-container td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfter.get(0)).isNotEqualTo(firstBefore);
    }

    @Test
    void editNoteOnRecommendation() throws Exception {
        page.navigate("http://localhost:" + getPort() + "/r.html");
        page.waitForLoadState();

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();

        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        page.reload();
        page.waitForLoadState();
        page.waitForSelector(".table-container table");

        var firstRow = page.locator(".table-container tbody tr").first();

        // Hover over the row to reveal the edit button
        firstRow.hover();
        firstRow.locator("button:has-text('Edit')").click();

        // Verify the dialog is open and empty
        assertThat(page.locator("#edit-dialog").getAttribute("open")).isNotNull();
        assertThat(page.locator("#edit-dialog-comment").inputValue()).isEmpty();

        // Save a note
        page.locator("#edit-dialog-comment").fill("Recommended!");
        page.waitForResponse(r -> r.url().contains("/htmx/user/comment"), () -> page.locator("#edit-dialog-save")
                .click());

        // Verify the row was updated inline
        var commentsDiv = firstRow.locator(".comments");
        PlaywrightAssertions.assertThat(commentsDiv).isVisible();
        assertThat(commentsDiv.textContent()).contains("Recommended!");
    }
}

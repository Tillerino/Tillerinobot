package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.tillerino.ppaddict.server.PersistentUserData;
import tillerino.tillerinobot.MockData;

class NotesTest extends AbstractPlaywrightTest {

    @Test
    void notesDisplayInTable() throws Exception {
        // Pre-populate a comment for the first mock beatmap
        int beatmapId = MockData.getSetIds().keySet().iterator().next();
        PersistentUserData userData = new PersistentUserData();
        userData.setBeatmapComments(new TreeSet<>());
        userData.putBeatMapComment(beatmapId, 0, "Great map!");

        try {
            userDataService.saveUserData("local:TestUser", userData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Login
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

        // Navigate to the beatmap page
        page.navigate("http://localhost:" + getPort() + "/v2.html?b=" + beatmapId);
        page.waitForSelector("table.beatmaps");

        // Verify the comment is displayed
        var commentsDiv = page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods=''] .comments");
        PlaywrightAssertions.assertThat(commentsDiv).isVisible();
        String commentText = (String)
                commentsDiv.evaluate(
                        "el => { let clone = el.cloneNode(true); let span = clone.querySelector('span'); if (span) span.remove(); return clone.textContent.trim(); }");
        assertThat(commentText).contains("Great map!");
    }

    @Test
    void editDialogOpensAndSaves() throws Exception {
        int beatmapId = MockData.getSetIds().keySet().iterator().next();
        PersistentUserData userData = new PersistentUserData();
        userData.setBeatmapComments(new TreeSet<>());
        userData.putBeatMapComment(beatmapId, 0, "Original note");

        try {
            userDataService.saveUserData("local:TestUser", userData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // Navigate to the beatmap page
        page.navigate("http://localhost:" + getPort() + "/v2.html?b=" + beatmapId);
        page.waitForSelector("table.beatmaps");

        // Hover over the row to reveal the edit button
        page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods='']")
                .hover();

        // Click the edit button
        page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods=''] button:has-text('Edit')")
                .click();

        // Verify the dialog is open and pre-filled
        assertThat(page.locator("#edit-dialog").getAttribute("open")).isNotNull();
        assertThat(page.locator("#edit-dialog-comment").inputValue()).isEqualTo("Original note");

        // Update the note
        page.locator("#edit-dialog-comment").fill("Updated note");
        page.locator("#edit-dialog-save").click();
        page.waitForResponse(r -> r.url().contains("/htmx/user/comment"), () -> {});

        // Verify the row was updated inline
        var commentsDiv = page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods=''] .comments");
        PlaywrightAssertions.assertThat(commentsDiv).isVisible();
        String updatedComment = (String)
                commentsDiv.evaluate(
                        "el => { let clone = el.cloneNode(true); let span = clone.querySelector('span'); if (span) span.remove(); return clone.textContent.trim(); }");
        assertThat(updatedComment).contains("Updated note");
    }

    @Test
    void notesFilterShowsOnlyMatchingNotes() throws Exception {
        int beatmapId1 = MockData.getSetIds().keySet().iterator().next();
        int beatmapId2 = 0;
        for (int id : MockData.getSetIds().keySet()) {
            if (id != beatmapId1) {
                beatmapId2 = id;
                break;
            }
        }

        PersistentUserData userData = new PersistentUserData();
        userData.setBeatmapComments(new TreeSet<>());
        userData.putBeatMapComment(beatmapId1, 0, "Contains this word");
        userData.putBeatMapComment(beatmapId2, 0, "Different note here");

        try {
            userDataService.saveUserData("local:TestUser", userData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // Apply the notes filter
        page.locator(".pager-row td:first-child button").click();
        page.locator("input[name='commentFilter']").fill("this word");
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {
            page.locator("input[name='commentFilter']").dispatchEvent("change");
        });

        // Verify only the beatmap with the matching note is shown
        List<String> visibleBeatmapIds = page.locator("tbody tr").all().stream()
                .map(el -> el.getAttribute("data-beatmapid"))
                .toList();
        assertThat(visibleBeatmapIds).containsExactly(String.valueOf(beatmapId1));
    }

    @Test
    void notesFilterAnyButtonClearsFilter() throws Exception {
        int beatmapId1 = MockData.getSetIds().keySet().iterator().next();
        int beatmapId2 = 0;
        for (int id : MockData.getSetIds().keySet()) {
            if (id != beatmapId1) {
                beatmapId2 = id;
                break;
            }
        }

        PersistentUserData userData = new PersistentUserData();
        userData.setBeatmapComments(new TreeSet<>());
        userData.putBeatMapComment(beatmapId1, 0, "Some note");
        userData.putBeatMapComment(beatmapId2, 0, "Another note");

        try {
            userDataService.saveUserData("local:TestUser", userData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // Apply the notes filter
        page.locator(".pager-row td:first-child button").click();
        page.locator("input[name='commentFilter']").fill("note");
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {
            page.locator("input[name='commentFilter']").dispatchEvent("change");
        });

        // Verify both beatmaps are shown (both have "note" in their comment)
        List<String> beforeAny = page.locator("tbody tr").all().stream()
                .map(el -> el.getAttribute("data-beatmapid"))
                .toList();
        assertThat(beforeAny).hasSize(2);

        // Click the "(any)" button
        page.locator("input[name='commentFilter']").evaluate("el => el.nextElementSibling.click()");

        // Verify both beatmaps are still shown (any = *)
        List<String> afterAny = page.locator("tbody tr").all().stream()
                .map(el -> el.getAttribute("data-beatmapid"))
                .toList();
        assertThat(afterAny).hasSize(2);
    }

    @Test
    void removingNoteViaEditDialog() throws Exception {
        int beatmapId = MockData.getSetIds().keySet().iterator().next();
        PersistentUserData userData = new PersistentUserData();
        userData.setBeatmapComments(new TreeSet<>());
        userData.putBeatMapComment(beatmapId, 0, "Will be removed");

        try {
            userDataService.saveUserData("local:TestUser", userData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        page.navigate("http://localhost:" + getPort() + "/v2.html?b=" + beatmapId);
        page.waitForSelector("table.beatmaps");

        page.locator("#user-area button:has-text('Login')").click();
        page.locator("#login-modal a").click();
        page.waitForLoadState();
        page.fill("input[name='username']", "TestUser");
        page.locator("input[type='submit']").click();
        page.waitForLoadState();
        page.waitForSelector("#user-area .username");

        // Hover over the row to reveal the edit button
        page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods='']")
                .hover();

        // Click the edit button
        page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods=''] button:has-text('Edit')")
                .click();

        // Clear the note and save
        page.locator("#edit-dialog-comment").fill("");
        page.locator("#edit-dialog-save").click();

        // Verify the comment div was removed
        var commentsDiv = page.locator("tbody tr[data-beatmapid='" + beatmapId + "'][data-mods=''] .comments");
        PlaywrightAssertions.assertThat(commentsDiv).not().isVisible();
    }
}

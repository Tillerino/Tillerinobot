package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.util.List;
import org.junit.jupiter.api.Test;

class FiltersTest extends AbstractPlaywrightTest {

    @Test
    void filterShowHideToggle() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isHidden();

        page.locator(".pager-row td:first-child button").click();

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isVisible();

        PlaywrightAssertions.assertThat(page.locator(".filter-row input").first())
                .isVisible();

        page.locator("tr.filter-row button").click();

        PlaywrightAssertions.assertThat(page.locator(".filter-row")).isHidden();
    }

    @Test
    void filterInputsExistWithCorrectDefaults() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        page.locator(".pager-row td:first-child button").click();

        // 8 numeric filter fields x 2 inputs (min + max) + 1 name filter + 1 checkbox = 18 inputs
        var allInputs = page.locator(".filter-row input");
        PlaywrightAssertions.assertThat(allInputs).hasCount(18);

        // expectedPP - first .threecharminmaxcell cell, integer defaults to 0
        var threeCharCells = page.locator(".filter-row td.threecharminmaxcell");
        PlaywrightAssertions.assertThat(threeCharCells).hasCount(7);
        var expectedPPInputs = threeCharCells.nth(0).locator("input");
        PlaywrightAssertions.assertThat(expectedPPInputs.nth(0)).hasValue("0");
        PlaywrightAssertions.assertThat(expectedPPInputs.nth(1)).hasValue("0");

        // perfectPP - second .threecharminmaxcell cell, integer defaults to 0
        var perfectPPInputs = threeCharCells.nth(1).locator("input");
        PlaywrightAssertions.assertThat(perfectPPInputs.nth(0)).hasValue("0");
        PlaywrightAssertions.assertThat(perfectPPInputs.nth(1)).hasValue("0");

        // AR - third .threecharminmaxcell cell, decimal defaults to 0.0
        var arInputs = threeCharCells.nth(2).locator("input");
        PlaywrightAssertions.assertThat(arInputs.nth(0)).hasValue("0.0");
        PlaywrightAssertions.assertThat(arInputs.nth(1)).hasValue("0.0");

        // starDiff - sixth .threecharminmaxcell cell, decimal defaults to 0.0
        var starDiffInputs = threeCharCells.nth(5).locator("input");
        PlaywrightAssertions.assertThat(starDiffInputs.nth(0)).hasValue("0.0");
        PlaywrightAssertions.assertThat(starDiffInputs.nth(1)).hasValue("0.0");

        // BPM - seventh .threecharminmaxcell cell, integer defaults to 0
        var bpmInputs = threeCharCells.nth(6).locator("input");
        PlaywrightAssertions.assertThat(bpmInputs.nth(0)).hasValue("0");
        PlaywrightAssertions.assertThat(bpmInputs.nth(1)).hasValue("0");

        // mapLength - .fivecharminmaxcell cell, time, defaults to 0:00
        var mapLengthCells = page.locator(".filter-row td.fivecharminmaxcell");
        PlaywrightAssertions.assertThat(mapLengthCells).hasCount(1);
        PlaywrightAssertions.assertThat(mapLengthCells.locator("input").nth(0)).hasValue("0:00");
        PlaywrightAssertions.assertThat(mapLengthCells.locator("input").nth(1)).hasValue("0:00");
    }

    @Test
    void arMinFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(7)",
                ".filter-row td:nth-of-type(7) input:nth-of-type(1)",
                "6.5",
                text -> Double.parseDouble(text.replace("AR", "")) >= 6.5,
                text -> assertThat(Double.parseDouble(text.replace("AR", ""))).isGreaterThanOrEqualTo(6.5));
    }

    @Test
    void arMaxFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(7)",
                ".filter-row td:nth-of-type(7) input:nth-of-type(2)",
                "6.5",
                text -> Double.parseDouble(text.replace("AR", "")) <= 6.5,
                text -> assertThat(Double.parseDouble(text.replace("AR", ""))).isLessThanOrEqualTo(6.5));
    }

    @Test
    void bpmMinFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(11)",
                ".filter-row td:nth-of-type(11) input:nth-of-type(1)",
                "250",
                text -> Double.parseDouble(text) >= 250,
                text -> assertThat(Double.parseDouble(text)).isGreaterThanOrEqualTo(250));
    }

    @Test
    void bpmMaxFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(11)",
                ".filter-row td:nth-of-type(11) input:nth-of-type(2)",
                "250",
                text -> Double.parseDouble(text) <= 250,
                text -> assertThat(Double.parseDouble(text)).isLessThanOrEqualTo(250));
    }

    @Test
    void mapLengthMinFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(12)",
                ".filter-row td:nth-of-type(12) input:nth-of-type(1)",
                "3:00",
                text -> toSeconds(text) >= 180,
                text -> assertThat(toSeconds(text)).isGreaterThanOrEqualTo(180));
    }

    @Test
    void mapLengthMaxFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(12)",
                ".filter-row td:nth-of-type(12) input:nth-of-type(2)",
                "3:00",
                text -> toSeconds(text) <= 180,
                text -> assertThat(toSeconds(text)).isLessThanOrEqualTo(180));
    }

    @Test
    void nameFilterActuallyReducesRows() {
        applyFilter(
                "tbody tr td:nth-of-type(4) a",
                ".filter-row td:nth-of-type(4) input.namefilter",
                "Hatsune",
                text -> text.toLowerCase().contains("hatsune"),
                text -> assertThat(text.toLowerCase()).contains("hatsune"));
    }

    @Test
    void rankedOnlyFilterReducesRows() {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");

        page.locator(".pager-row td:first-child button").click();

        var rankedCheckbox = page.locator(".filter-row td:first-child input[type=checkbox]");
        var namesBefore = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesBefore).isNotEmpty();

        rankedCheckbox.click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesAfter = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesAfter).isNotEmpty();
        assertThat(namesAfter).isNotEqualTo(namesBefore);

        rankedCheckbox.click();
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {});

        var namesRestored = page.locator("tbody tr td:nth-of-type(4) a").allTextContents();
        assertThat(namesRestored).isEqualTo(namesBefore);
    }

    private static int toSeconds(String text) {
        String[] parts = text.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /** Applies a numeric filter on a column and verifies all visible rows satisfy the constraint. */
    private void applyFilter(
            String columnSelector,
            String filterInputSelector,
            String filterValue,
            java.util.function.Predicate<String> insideRange,
            java.util.function.Consumer<String> assertRow) {
        page.navigate("http://localhost:" + getPort() + "/v2.html");
        page.waitForSelector("table.beatmaps");
        page.locator(".pager-row td:first-child button").click();

        // Pre-filter: verify values exist both inside and outside the filter range
        List<String> texts = page.locator(columnSelector).allTextContents();
        long insideCount = texts.stream().filter(insideRange).count();
        assertThat(insideCount).isGreaterThan(0);
        assertThat(texts.size() - insideCount).isGreaterThan(0);

        // Apply the filter
        var filterInput = page.locator(filterInputSelector);
        filterInput.fill(filterValue);
        page.waitForResponse("**/htmx/beatmaps/update**", () -> {
            filterInput.dispatchEvent("change");
        });

        // Post-filter: verify all visible rows are inside the filter range
        List<String> filtered = page.locator(columnSelector).allTextContents();
        assertThat(filtered).isNotEmpty();
        filtered.forEach(assertRow);
    }
}

package org.tillerino.ppaddict.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.*;

@Path("/beatmaps")
public class BeatmapTableResource {

    private static final DecimalFormat format = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat percentageFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat ppFormat = new DecimalFormat("#pp", new DecimalFormatSymbols(Locale.US));

    private final BeatmapTableServiceImpl beatmapTableService;
    private final UserDataServiceImpl userDataService;

    @Inject
    public BeatmapTableResource(BeatmapTableServiceImpl beatmapTableService, UserDataServiceImpl userDataService) {
        this.beatmapTableService = beatmapTableService;
        this.userDataService = userDataService;
    }

    @GET
    @Path("/initial")
    @Produces(MediaType.TEXT_HTML)
    public String getInitialTable(@Context HttpServletRequest httpServletRequest)
            throws PpaddictException, JsonProcessingException {
        UserCredentials user = getUserDataAndCredentials(httpServletRequest);
        PersistentUserData userData = user.userData;
        Credentials credentials = user.credentials;
        BeatmapRangeRequest request = userData != null ? userData.getLastRequest() : null;
        if (request == null) {
            request = new BeatmapRangeRequest();
        }
        String table = execute(false, request, credentials, userData);
        return table
                + "<script>window.__rangeRequest = %s</script>"
                        .formatted(new ObjectMapper().writeValueAsString(request));
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public String getTableUpdate(
            @Context HttpServletRequest httpServletRequest,
            @QueryParam("onlyRows") boolean onlyRows,
            BeatmapRangeRequest request)
            throws PpaddictException {
        UserCredentials user = getUserDataAndCredentials(httpServletRequest);
        return execute(onlyRows, request, user.credentials, user.userData);
    }

    private UserCredentials getUserDataAndCredentials(HttpServletRequest httpServletRequest) throws PpaddictException {
        PersistentUserData userData = null;
        Credentials credentials = userDataService.getCredentials(httpServletRequest);
        if (credentials != null) {
            userData = userDataService.getServerUserData(credentials);
        }
        return new UserCredentials(credentials, userData);
    }

    private String execute(
            boolean onlyRows,
            BeatmapRangeRequest request,
            @CheckForNull Credentials credentials,
            @CheckForNull PersistentUserData userData)
            throws PpaddictException {
        BeatmapBundle bundle = beatmapTableService.executeGetRange(request, credentials, userData);

        StringBuilder html = new StringBuilder();
        if (!onlyRows) {
            html.append("<div class=\"table-scroll\">");
            html.append(formatBeatmapsTableHeader(
                    userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS,
                    request.sortBy,
                    request.direction));
            html.append("<tbody>");
        }
        for (Beatmap beatmap : bundle.beatmaps) {
            formatBeatmapTablesRow(beatmap, html);
        }

        int nextStart = request.start + request.length;
        if (nextStart < bundle.available) {
            html.append(String.format("""
            <tr hx-post="/htmx/beatmaps/update?onlyRows=true"
                hx-ext="postrangerequest"
                hx-vals='js:{json:rangeReq({copy: { start: %s }})}'
                hx-trigger="intersect once"
                hx-swap="outerHTML">
              <td colspan="12" style="text-align:center; padding: 10px; color: gray;">
                Loading more...
              </td>
            </tr>""", nextStart));
        }

        if (!onlyRows) {
            html.append("</tbody>")
                    .append("<tfoot>")
                    .append(formatFilterRow(request))
                    .append(formatPagerRow(request, bundle.available))
                    .append("</tfoot></table></div>");
        }
        return html.toString();
    }

    record FilterField(String cssClass, boolean isDecimal, boolean isTime, MinMax range, String fieldName)
            implements Supplier<String> {

        private String formatValue(Integer value) {
            if (value == null) {
                return isTime ? "0:00" : (isDecimal ? "0.0" : "0");
            }
            if (isTime) {
                int minutes = value / 60;
                int seconds = value % 60;
                return minutes + ":" + String.format("%02d", seconds);
            }
            if (isDecimal) {
                return String.valueOf(value / 100.0);
            }
            return String.valueOf(value);
        }

        private String toValueJs() {
            if (isDecimal) return "Math.round(parseFloat(this.value)*100)";
            if (isTime) return "toTime(this.value)";
            return "parseInt(this.value,10)";
        }

        @Override
        public String get() {
            String js = toValueJs();
            return String.format(
                    """
              <td class="%s">
                &ge;<input type="text" hx-post="/htmx/beatmaps/update"
                  hx-ext="postrangerequest" hx-trigger="change"
                  hx-vals='js:{json:rangeReq({mod:{%s: {min: %s}}})}'
                  hx-target=".table-container"
                  class="chillinput %s" value="%s" tabindex="-1" />
                <br />&le;<input type="text" hx-post="/htmx/beatmaps/update"
                  hx-ext="postrangerequest" hx-trigger="change"
                  hx-vals='js:{json:rangeReq({mod:{%s: {max: %s}}})}'
                  hx-target=".table-container"
                  class="chillinput %s" value="%s" tabindex="-1" />
              </td>
              """,
                    cssClass,
                    fieldName,
                    js,
                    cssClass,
                    formatValue(range.min),
                    fieldName,
                    js,
                    cssClass,
                    formatValue(range.max));
        }
    }

    private static String formatFilterRow(BeatmapRangeRequest request) {
        List<Supplier<String>> fields = Arrays.asList(
                new FilterField("threecharminmaxcell", false, false, request.expectedPP, "expectedPP"),
                new FilterField("threecharminmaxcell", false, false, request.perfectPP, "perfectPP"),
                () -> formatNameFilter(request.getSearches().getSafeSearchText()),
                () -> "<td></td>",
                () -> "<td></td>",
                new FilterField("threecharminmaxcell", true, false, request.aR, "aR"),
                new FilterField("threecharminmaxcell", true, false, request.oD, "oD"),
                new FilterField("threecharminmaxcell", true, false, request.cS, "cS"),
                new FilterField("threecharminmaxcell", true, false, request.starDiff, "starDiff"),
                new FilterField("threecharminmaxcell", false, false, request.bpm, "bpm"),
                new FilterField("fivecharminmaxcell", false, true, request.mapLength, "mapLength"));

        StringBuilder html = new StringBuilder();
        html.append("<tr class=\"filter-row\">\n");

        html.append(String.format("""
                <td>
                  <label><input type="checkbox" %s
                    hx-post="/htmx/beatmaps/update"
                    hx-ext="postrangerequest"
                    hx-trigger="change"
                    hx-vals='js:{json:rangeReq({mod:{rankedOnly:this.checked}})}'
                    hx-target=".table-container" class="namefilter" /> ranked only</label>
                  <br />
                  <button type="button" onclick="modifyRule('.filter-row', 'display', 'none')">hide filters</button>
                </td>""", request.rankedOnly ? "checked" : ""));

        for (Supplier<String> field : fields) {
            html.append(field.get());
        }
        html.append("</tr>\n");
        return html.toString();
    }

    private static String formatNameFilter(String value) {
        return String.format("""
          <td class="namefiltercell">
            <input type="text" size="25" hx-post="/htmx/beatmaps/update"
              hx-ext="postrangerequest" hx-trigger="change"
              hx-vals='js:{json:rangeReq({mod:{searches:{searchText:this.value}}})}'
              hx-target=".table-container"
              class="namefilter" value="%s" tabindex="-1" />
          </td>
          """, value);
    }

    private static String formatPagerRow(BeatmapRangeRequest request, int available) {
        int length = request.length;

        return """
            <tr class="pager-row">
              <td><button type="button" onclick="modifyRule('.filter-row', 'display', '')">show filters</button></td>
              <td colspan="10" class="pager">%s %s %d-%d of %d %s %s %s</td>
              <td></td>
            </tr>""".formatted(
                        pageButton(0, request, available, "⇤"),
                        pageButton(Math.max(0, request.start - length), request, available, "←"),
                        request.start + 1,
                        Math.min(request.start + length, available),
                        available,
                        pageButton(request.start + length, request, available, "→"),
                        pageButton(request.start + 10 * length, request, available, "↠"),
                        pageButton(available - length, request, available, "⇥"));
    }

    private static String pageButton(int pageStart, BeatmapRangeRequest request, int available, String label) {
        boolean enabled = pageStart >= 0 && pageStart < available && pageStart != request.start;
        if (enabled) {
            return String.format("""
                    <button hx-post="/htmx/beatmaps/update"
                      hx-ext="postrangerequest"
                      hx-vals='js:{json: rangeReq({mod:{ start: %s }})}'
                      hx-target=".table-container">%s</button>""", pageStart, label);
        }
        return "<button disabled>" + label + "</button>";
    }

    private static StringBuilder formatBeatmapsTableHeader(
            Settings settings, BeatmapRangeRequest.Sort currentSort, int ss) {
        String lowAcc = percentageFormat.format(settings.getLowAccuracy()) + "%";
        String highAcc = percentageFormat.format(settings.getHighAccuracy()) + "%";

        record Col(String label, BeatmapRangeRequest.Sort sortKey, String width) {}

        Col[] cols = {
            new Col("", null, "117px"),
            new Col(lowAcc, BeatmapRangeRequest.Sort.EXPECTED, "75px"),
            new Col(highAcc, BeatmapRangeRequest.Sort.PERFECT, "75px"),
            new Col("", null, null),
            new Col("", null, "48px"),
            new Col("", null, "30px"),
            new Col("AR", null, "75px"),
            new Col("OD", null, "75px"),
            new Col("CS", null, "75px"),
            new Col("diff", BeatmapRangeRequest.Sort.STAR_DIFF, "75px"),
            new Col("BPM", BeatmapRangeRequest.Sort.BPM, "75px"),
            new Col("length", BeatmapRangeRequest.Sort.LENGTH, "90px")
        };

        StringBuilder html =
                new StringBuilder().append("<table class=\"beatmaps\">").append("<colgroup>");
        for (Col col : cols) {
            if (col.width != null) {
                html.append(String.format("<col style=\"width: %s;\">", col.width));
            } else {
                html.append("<col>");
            }
        }
        html.append("</colgroup>").append("<thead><tr>");

        for (Col col : cols) {
            if (col.sortKey != null) {
                boolean isActive = col.sortKey == currentSort;
                String arrow = isActive ? (ss == 1 ? "↓" : "↑") : "";
                Integer nextDirection = isActive ? (ss == 1 ? -1 : null) : (Integer) 1;
                String nextSortKey = nextDirection != null ? "\"" + col.sortKey.name() + "\"" : null;
                html.append(String.format("""
                      <th class="numeric-cell">%s<button type="button"
                        hx-post="/htmx/beatmaps/update"
                        hx-ext="postrangerequest"
                        hx-vals='js:{json: rangeReq({mod:{ sortBy: %s, direction: %s, start: 0 }})}'
                        hx-target=".table-container">%s</button></th>""", arrow, nextSortKey, nextDirection, col.label));
            } else if (col.label.isEmpty()) {
                html.append("<th></th>");
            } else {
                html.append("<th class=\"numeric-cell\">").append(col.label).append("</th>");
            }
        }

        html.append("</tr></thead>");
        return html;
    }

    private static void formatBeatmapTablesRow(Beatmap beatmap, StringBuilder html) {
        // one append per column
        html.append("<tr>")
                .append("""
                    <td>
                      <a href="http://osu.ppy.sh/beatmapsets/%d/download"><img src="//b.ppy.sh/thumb/%d.jpg" height="60" loading="lazy" style="vertical-align:middle"></a>
                      <a href="osu://b/%d"><img src="/osuDownloadDirect.png" height="60" width="10" style="vertical-align:middle"></a>
                    </td>""".formatted(beatmap.setid, beatmap.setid, beatmap.beatmapid))
                .append("<td class=\"numeric-cell\">" + ppFormat.format(beatmap.lowPP) + "</td>")
                .append("<td class=\"numeric-cell\">" + ppFormat.format(beatmap.highPP) + "</td>")
                .append("""
                    <td>
                      <a href="http://osu.ppy.sh/b/%s" target="_blank">%s - %s [%s]</a>
                    </td>""".formatted(beatmap.beatmapid, beatmap.artist, beatmap.title, beatmap.version))
                .append("<td></td>")
                .append("<td></td>")
                .append("<td class=\"numeric-cell\">AR" + format.format(beatmap.approachRate) + "</td>")
                .append("<td class=\"numeric-cell\">OD" + format.format(beatmap.overallDiff) + "</td>")
                .append("<td class=\"numeric-cell\">CS" + format.format(beatmap.circleSize) + "</td>")
                .append("<td class=\"numeric-cell\">"
                        + (beatmap.starDifficulty != null ? format.format(beatmap.starDifficulty) : "N/A") + "</td>")
                .append("<td class=\"numeric-cell\">" + (int) beatmap.bpm + "</td>")
                .append("<td class=\"numeric-cell\">" + beatmap.getFormattedLength() + "</td>")
                .append("</tr>");
    }

    private record UserCredentials(
            @CheckForNull Credentials credentials,
            @CheckForNull PersistentUserData userData) {}
}

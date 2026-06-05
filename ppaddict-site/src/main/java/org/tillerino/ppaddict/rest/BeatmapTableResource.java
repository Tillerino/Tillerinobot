package org.tillerino.ppaddict.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
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
    public String getInitialTable(HttpServletRequest httpServletRequest)
            throws PpaddictException, JsonProcessingException {
        PersistentUserData userData = getUserData(httpServletRequest);
        BeatmapRangeRequest request = userData != null ? userData.getLastRequest() : null;
        if (request == null) {
            request = new BeatmapRangeRequest();
        }
        String table = execute(false, request, userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS);
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
        PersistentUserData userData = getUserData(httpServletRequest);

        return execute(onlyRows, request, userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS);
    }

    @CheckForNull
    private PersistentUserData getUserData(HttpServletRequest httpServletRequest) throws PpaddictException {
        PersistentUserData userData = null;
        var credentials = userDataService.getCredentials(httpServletRequest);
        if (credentials != null) {
            userData = userDataService.getServerUserData(credentials);
        }
        return userData;
    }

    private String execute(boolean onlyRows, BeatmapRangeRequest request, Settings settings) throws PpaddictException {
        BeatmapBundle bundle = beatmapTableService.getRange(request);

        StringBuilder html = new StringBuilder();
        if (!onlyRows) {
            html.append("<div class=\"table-scroll\">");
            html.append(formatBeatmapsTableHeader(settings, request.sortBy, request.direction));
        }
        for (var beatmap : bundle.beatmaps) {
            formatBeatmapTablesRow(beatmap, html);
        }

        int nextStart = request.start + request.length;
        if (nextStart < bundle.available) {
            html.append(String.format("""
            <tr hx-post="/htmx/beatmaps/update?onlyRows=true"
                hx-ext="postrangerequest"
                hx-vals='{ "start": %s }'
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

    record FilterField(String cssClass, boolean isDecimal, boolean isTime, MinMax range) {
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
                return format.format(value / 100.0);
            }
            return String.valueOf(value);
        }

        String asCell() {
            return String.format("""
              <td class="%s">
                &ge;<input type="text" class="chillinput %s" value="%s" tabindex="-1" />
                <br />&le;<input type="text" class="chillinput %s" value="%s" tabindex="-1" />
              </td>
              """, cssClass, cssClass, formatValue(range.min), cssClass, formatValue(range.max));
        }
    }

    private static String formatFilterRow(BeatmapRangeRequest request) {
        FilterField[] fields = {
            new FilterField("threecharminmaxcell", false, false, request.expectedPP),
            new FilterField("threecharminmaxcell", false, false, request.perfectPP),
            null, // col 3: empty
            null, // col 4: empty
            null, // col 5: empty
            new FilterField("threecharminmaxcell", true, false, request.aR),
            new FilterField("threecharminmaxcell", true, false, request.oD),
            new FilterField("threecharminmaxcell", true, false, request.cS),
            new FilterField("threecharminmaxcell", true, false, request.starDiff),
            new FilterField("threecharminmaxcell", false, false, request.bpm),
            new FilterField("fivecharminmaxcell", false, true, request.mapLength)
        };

        StringBuilder html = new StringBuilder();
        html.append("<tr class=\"filter-row\">\n");

        html.append(
                "<td><br /><button type=\"button\" onclick=\"modifyRule('.filter-row', 'display', 'none')\">hide filters</button></td>");
        for (FilterField field : fields) {
            html.append(field == null ? "<td></td>" : field.asCell());
        }
        html.append("</tr>\n");
        return html.toString();
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
                      hx-vals='{ "start": %s }'
                      hx-target=".table-container">%s</button>""", pageStart, label);
        }
        return "<span>" + label + "</span>";
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
                int direction = isActive ? (ss == 1 ? -1 : 1) : 1;
                String arrow = isActive ? (direction == 1 ? "↑" : "↓") : "";
                html.append(String.format("""
                      <th class="numeric-cell">%s<button type="button"
                        hx-post="/htmx/beatmaps/update"
                        hx-ext="postrangerequest"
                        hx-vals='{ "sortBy": "%s", "direction": %s, "start": 0 }'
                        hx-target=".table-container">%s</button></th>""", arrow, col.sortKey.name(), direction, col.label));
            } else if (col.label.isEmpty()) {
                html.append("<th></th>");
            } else {
                html.append("<th class=\"numeric-cell\">").append(col.label).append("</th>");
            }
        }

        html.append("</tr></thead><tbody");
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
}

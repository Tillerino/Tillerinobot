package org.tillerino.ppaddict.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.inject.Inject;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
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
    @Path("/table")
    @Produces(MediaType.TEXT_HTML)
    public String getTable(
            HttpServletRequest httpServletRequest,
            @QueryParam("start") int start,
            @QueryParam("onlyRows") boolean onlyRows,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("ss") int ss)
            throws PpaddictException {
        BeatmapRangeRequest request = new BeatmapRangeRequest();
        request.start = start;
        request.direction = ss;

        if (sortBy != null && !sortBy.isEmpty()) {
            try {
                request.sortBy = BeatmapRangeRequest.Sort.valueOf(sortBy);
            } catch (IllegalArgumentException e) {
                // ignore invalid sortBy
            }
        }

        Settings settings = Settings.DEFAULT_SETTINGS;
        var credentials = userDataService.getCredentials(httpServletRequest);
        if (credentials != null) {
            settings = userDataService.getServerUserData(credentials).getSettings();
        }

        BeatmapBundle bundle = beatmapTableService.getRange(request);

        StringBuilder html = onlyRows ? new StringBuilder() : formatBeatmapsTableHeader(settings, request.sortBy, ss);
        for (var beatmap : bundle.beatmaps) {
            formatBeatmapTablesRow(beatmap, html);
        }

        int nextStart = start + request.length;
        if (nextStart < bundle.available) {
            StringBuilder url = new StringBuilder(
                    String.format("/htmx/beatmaps/table?start=%d&length=%d&onlyRows=true", nextStart, request.length));
            if (request.sortBy != null) {
                url.append("&sortBy=").append(request.sortBy);
            }
            url.append("&ss=").append(ss);
            html.append(String.format("""
                <tr hx-get="%s"
                    hx-trigger="intersect once"
                    hx-swap="outerHTML">
                  <td colspan="12" style="text-align:center; padding: 10px; color: gray;">
                    Loading more...
                  </td>
                </tr>""", url));
        }

        if (!onlyRows) {
            html.append("</tbody></table>");
        }
        return html.toString();
    }

    private record Col(String label, BeatmapRangeRequest.Sort sortKey, String width) {}

    private static StringBuilder formatBeatmapsTableHeader(
            Settings settings, BeatmapRangeRequest.Sort currentSort, int ss) {
        String lowAcc = percentageFormat.format(settings.getLowAccuracy()) + "%";
        String highAcc = percentageFormat.format(settings.getHighAccuracy()) + "%";

        Col[] cols = {
            new Col("", null, "117px"),
            new Col(lowAcc, BeatmapRangeRequest.Sort.EXPECTED, "75px"),
            new Col(highAcc, BeatmapRangeRequest.Sort.PERFECT, "75px"),
            new Col("", null, null),
            new Col("", null, "48px"),
            new Col("", null, "30px"),
            new Col("AR", null, "75px"),
            new Col("OD", null, "75px"),
            new Col("CS", null, "55px"),
            new Col("diff", BeatmapRangeRequest.Sort.STAR_DIFF, "65px"),
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
                String sortByEncoded = URLEncoder.encode(col.sortKey.name(), StandardCharsets.UTF_8);
                String arrow = isActive ? (direction == 1 ? "↑" : "↓") : "";
                html.append(String.format(
                        "<th class=\"numeric-cell\">%s<button type=\"button\" hx-get=\"/htmx/beatmaps/table?sortBy=%s&ss=%d\" hx-target=\".table-container\">%s</button></th>",
                        arrow, sortByEncoded, direction, col.label));
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

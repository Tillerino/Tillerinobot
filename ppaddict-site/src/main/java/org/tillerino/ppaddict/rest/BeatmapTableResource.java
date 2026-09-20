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
import lombok.RequiredArgsConstructor;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.*;

@Path("/beatmaps")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BeatmapTableResource {

    static final DecimalFormat format = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
    static final DecimalFormat percentageFormat = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
    static final DecimalFormat ppFormat = new DecimalFormat("#pp", new DecimalFormatSymbols(Locale.US));

    private final BeatmapTableServiceImpl beatmapTableService;
    private final UserDataServiceImpl userDataService;

    @GET
    @Path("/initial")
    @Produces(MediaType.TEXT_HTML)
    public String getInitialTable(
            @Context HttpServletRequest httpServletRequest,
            @QueryParam("s") Integer setId,
            @QueryParam("b") Integer beatmapId)
            throws PpaddictException, JsonProcessingException {
        UserCredentials user = getUserDataAndCredentials(httpServletRequest);
        PersistentUserData userData = user.userData;
        Credentials credentials = user.credentials;
        BeatmapRangeRequest request = userData != null && userData.getLastRequest() != null
                ? userData.getLastRequest()
                : new BeatmapRangeRequest();
        request.getSearches().setSetId(setId);
        request.getSearches().setBeatmapId(beatmapId);
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
        if (request == null) {
            request = new BeatmapRangeRequest();
        }
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
                    true,
                    request.sortBy,
                    request.direction));
            html.append("<tbody>");
        }
        boolean isLoggedIn = userData != null;
        for (Beatmap beatmap : bundle.beatmaps) {
            formatBeatmapTablesRow(
                    beatmap,
                    html,
                    isLoggedIn,
                    userData != null && userData.getSettings().isOpenDirectOnMapSelect(),
                    MORE_DIALOG_BUTTON);
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
                    .append(formatFilterRow(request, isLoggedIn))
                    .append(formatPagerRow(request, bundle.available))
                    .append("</tfoot></table></div>")
                    .append(formatMoreDialog());
            if (userData != null) {
                html.append(formatEditDialog());
            }
        }
        return html.toString();
    }

    private String formatMoreDialog() {
        return """
        <dialog id="more-dialog" closedby="any" top="bottom" right="right">
          <a id="more-dialog-beatmap-link">Show all mods</a>
          <br />
          <a id="more-dialog-set-link">Show entire set</a>
        </dialog>""";
    }

    static String formatEditDialog() {
        return """
        <dialog id="edit-dialog" class="modal" top="bottom" right="right">
          <form id="edit-dialog-form" hx-post="/htmx/user/comment" hx-swap="none"
            hx-on::after-request="this.closest('dialog').close()">
            <input type="hidden" id="edit-dialog-beatmapid" name="beatmapid" />
            <input type="hidden" id="edit-dialog-mods" name="mods" />
            <label>Notes: <input type="text" id="edit-dialog-comment" maxlength="64" name="comment" /></label>
            <br />
            <button type="submit" id="edit-dialog-save">Save</button>
          </form>
        </dialog>""";
    }

    private static final HelpEntry HELP_DOWNLOADS = new HelpEntry(
            "downloads",
            "Click the image to download the beatmap set as a .osz file."
                    + " If you're not logged in on osu.ppy.sh, you will be asked to login there.\n"
                    + "Click the osu!direct stripe to view this map in osu!direct."
                    + " This feature is only available to osu! supporters.\n"
                    + "Unfortunately, osu! direct will only navigate to the corresponding beatmap set."
                    + " If you are a supporter, you can"
                    + " <a href=\"https://osu.ppy.sh/forum/t/221897\" target=\"_blank\">vote here</a>"
                    + " to get more specific osu! direct links.",
            "below-right");
    private static final HelpEntry HELP_PP_VALUES = new HelpEntry(
            "pp values",
            "These values are the amount of pp that a Full Combo play (no misses and no slider breaks)"
                    + " with the displayed accuracies is worth before weightage. "
                    + "<a href=\"https://osu.ppy.sh/wiki/Performance_Points#Weightage_system\" target=\"_blank\">(more info)</a>",
            "below-right");
    private static final HelpEntry HELP_META_INFORMATION = new HelpEntry(
            "meta information",
            "This is a selection of available meta information about each beatmap.\n"
                    + "AR = approach rate\nOD = overall difficulty\nCS = circle size"
                    + "\ndiff = star difficulty\nBPM = beats per minute."
                    + "<a href=\"https://osu.ppy.sh/wiki/Song_Setup#Difficulty\" target=\"_blank\">(more info)</a>",
            "below-right");
    private static final HelpEntry HELP_NAME_FILTER = new HelpEntry(
            "name filter",
            "Search in the name of a beatmap. The name is formed as shown above combining artist, title and version."
                    + " This will override any other filters. You can change this behaviour if you're logged in.",
            "above-right");
    private static final HelpEntry HELP_RANGE_FILTER = new HelpEntry(
            "range filter",
            "You can limit the range of some of the values."
                    + " Click a boundary to change it! Leave a field empty to restore the original boundary.",
            "above-right");

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

    private static String formatFilterRow(BeatmapRangeRequest request, boolean isLoggedIn) {
        List<Supplier<String>> fields = Arrays.asList(
                new FilterField("threecharminmaxcell", false, false, request.expectedPP, "expectedPP"),
                new FilterField("threecharminmaxcell", false, false, request.perfectPP, "perfectPP"),
                () -> formatNameAndNotesFilter(request, isLoggedIn),
                () -> "<td></td>",
                () -> "<td" + HELP_RANGE_FILTER + "></td>",
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

    private static String formatNameAndNotesFilter(BeatmapRangeRequest request, boolean isLoggedIn) {
        return """
          <td class="namefiltercell"%s>
            Name: <input type="text" size="20" hx-post="/htmx/beatmaps/update"
              hx-ext="postrangerequest" hx-trigger="change"
              hx-vals='js:{json:rangeReq({mod:{searches:{searchText:this.value}}})}'
              hx-target=".table-container" class="namefilter" value="%s" tabindex="-1" />
            %s
          </td>
          """.formatted(
                        HELP_NAME_FILTER,
                        request.getSearches().getSafeSearchText(),
                        isLoggedIn ? formatNotesFilter(request.getSearches().getSafeSearchComment()) : "");
    }

    private static String formatNotesFilter(String value) {
        return """
          <br />
          Notes: <input type="text" name="commentFilter" size="20" hx-post="/htmx/beatmaps/update"
            hx-ext="postrangerequest" hx-trigger="change"
            hx-vals='js:{json:rangeReq({mod:{searches:{searchComment:this.value}}})}'
            hx-target=".table-container" class="commentfilter" value="%s" tabindex="-1" />
          <button type="button" onclick="var i=this.previousElementSibling;i.value='*';i.dispatchEvent(new Event('change'))">(any)</button>
          """.formatted(value);
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

    static StringBuilder formatBeatmapsTableHeader(
            Settings settings, boolean sortable, BeatmapRangeRequest.Sort currentSort, int ss) {
        String lowAcc = percentageFormat.format(settings.getLowAccuracy()) + "%";
        String highAcc = percentageFormat.format(settings.getHighAccuracy()) + "%";

        record Col(String label, BeatmapRangeRequest.Sort sortKey, String width, HelpEntry help) {}

        Col[] cols = {
            new Col("", null, "117px", HELP_DOWNLOADS),
            new Col(lowAcc, BeatmapRangeRequest.Sort.EXPECTED, "75px", null),
            new Col(highAcc, BeatmapRangeRequest.Sort.PERFECT, "75px", HELP_PP_VALUES),
            new Col("", null, null, null),
            new Col("", null, "48px", null),
            new Col("", null, "30px", null),
            new Col("AR", null, "75px", null),
            new Col("OD", null, "75px", HELP_META_INFORMATION),
            new Col("CS", null, "75px", null),
            new Col("diff", BeatmapRangeRequest.Sort.STAR_DIFF, "75px", null),
            new Col("BPM", BeatmapRangeRequest.Sort.BPM, "75px", null),
            new Col("length", BeatmapRangeRequest.Sort.LENGTH, "90px", null)
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
            String help = col.help() == null ? "" : col.help().toString();
            if (sortable && col.sortKey != null) {
                boolean isActive = col.sortKey == currentSort;
                String arrow = isActive ? (ss == 1 ? "↓" : "↑") : "";
                Integer nextDirection = isActive ? (ss == 1 ? -1 : null) : (Integer) 1;
                String nextSortKey = nextDirection != null ? "\"" + col.sortKey.name() + "\"" : null;
                html.append(String.format("""
                      <th class="numeric-cell"%s>%s<button type="button"
                        hx-post="/htmx/beatmaps/update"
                        hx-ext="postrangerequest"
                        hx-vals='js:{json: rangeReq({mod:{ sortBy: %s, direction: %s, start: 0 }})}'
                        hx-target=".table-container">%s</button></th>""", help, arrow, nextSortKey, nextDirection, col.label));
            } else if (col.label.isEmpty()) {
                html.append("<th").append(help).append("></th>");
            } else {
                html.append("<th class=\"numeric-cell\"")
                        .append(help)
                        .append(">")
                        .append(col.label)
                        .append("</th>");
            }
        }

        html.append("</tr></thead>");
        return html;
    }

    static final String MORE_DIALOG_BUTTON =
            "<td><button type=\"button\" command=\"show-modal\" commandfor=\"more-dialog\""
                    + " onclick=\"updateMoreDialogLinks(this.closest('tr'))\">...</button></td>";

    static void formatBeatmapTablesRow(
            Beatmap beatmap,
            StringBuilder html,
            boolean isLoggedIn,
            boolean openDirectOnMapSelect,
            String actionCellHtml) {
        String mods = beatmap.mods != null ? beatmap.mods : "";
        html.append(("<tr data-beatmapid=\"%d\" data-beatmapsetid=\"%d\" data-mods=\"%s\"%s>")
                .formatted(
                        beatmap.beatmapid,
                        beatmap.setid,
                        mods,
                        openDirectOnMapSelect
                                ? " onclick=\"if (!event.target.closest('a,button,input')) window.location.assign('osu://b/%d')\""
                                        .formatted(beatmap.beatmapid)
                                : ""));
        html.append(("<td>"
                        + "<a href=\"http://osu.ppy.sh/beatmapsets/%d/download\"><img src=\"//b.ppy.sh/thumb/%d.jpg\" height=\"60\" loading=\"lazy\" style=\"vertical-align:middle\"></a>"
                        + "<a href=\"osu://b/%d\"><img src=\"/osuDownloadDirect.png\" height=\"60\" width=\"10\" style=\"vertical-align:middle\"></a>"
                        + "</td>")
                .formatted(beatmap.setid, beatmap.setid, beatmap.beatmapid));
        html.append("<td class=\"numeric-cell\">" + ppFormat.format(beatmap.lowPP) + "</td>");
        html.append("<td class=\"numeric-cell\">" + ppFormat.format(beatmap.highPP) + "</td>");
        html.append(("<td>" + "<a href=\"http://osu.ppy.sh/b/%s\" target=\"_blank\">%s - %s [%s]</a> %s" + "%s"
                        + "</td>")
                .formatted(
                        beatmap.beatmapid, beatmap.artist, beatmap.title, beatmap.version, mods, formatNote(beatmap)));
        if (isLoggedIn) {
            html.append("<td>")
                    .append(
                            "<button type=\"button\" command=\"show-modal\" commandfor=\"edit-dialog\" onclick=\"updateEditDialog(this.closest('tr'))\">Edit</button>")
                    .append("</td>");
        } else {
            html.append("<td></td>");
        }
        html.append(actionCellHtml);
        html.append("<td class=\"numeric-cell\">AR" + format.format(beatmap.approachRate) + "</td>");
        html.append("<td class=\"numeric-cell\">OD" + format.format(beatmap.overallDiff) + "</td>");
        html.append("<td class=\"numeric-cell\">CS" + format.format(beatmap.circleSize) + "</td>");
        html.append("<td class=\"numeric-cell\">"
                + (beatmap.starDifficulty != null ? format.format(beatmap.starDifficulty) : "N/A") + "</td>");
        html.append("<td class=\"numeric-cell\">" + (int) beatmap.bpm + "</td>");
        html.append("<td class=\"numeric-cell\">" + beatmap.getFormattedLength() + "</td>");
        html.append("</tr>");
    }

    private static String formatNote(Beatmap beatmap) {
        String mods = beatmap.mods != null ? beatmap.mods : "";
        String comment = beatmap.personalization != null && beatmap.personalization.comment != null
                ? beatmap.personalization.comment.trim()
                : "";
        return comment.isEmpty()
                ? "<div class=\"comments\" data-beatmapid=\"%d\" data-mods=\"%s\"></div>"
                        .formatted(beatmap.beatmapid, mods)
                : "<div class=\"comments\" data-beatmapid=\"%d\" data-mods=\"%s\">%s<span class=\"commentsdate\">%s</span></div>"
                        .formatted(beatmap.beatmapid, mods, comment, beatmap.personalization.commentDate);
    }

    private record UserCredentials(
            @CheckForNull Credentials credentials,
            @CheckForNull PersistentUserData userData) {}
}

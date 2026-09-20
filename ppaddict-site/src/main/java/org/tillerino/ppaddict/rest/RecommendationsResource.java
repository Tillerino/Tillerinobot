package org.tillerino.ppaddict.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.RecommendationsServiceImpl;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.PpaddictException;

@Path("/recommendations")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class RecommendationsResource {

    private static final String HIDE_BUTTON_TEMPLATE =
            "<td><button type=\"button\" hx-post=\"/htmx/recommendations/hide\" "
                    + "hx-vals='js:{beatmapid: %d, mods: \"%s\"}' "
                    + "hx-target=\"closest tr\" hx-swap=\"outerHTML\">Hide</button></td>";

    private final RecommendationsServiceImpl recommendationsService;
    private final UserDataServiceImpl userDataService;

    @GET
    @Path("/initial")
    @Produces(MediaType.TEXT_HTML)
    public String getInitialTable(@Context HttpServletRequest httpServletRequest) throws PpaddictException {
        Credentials credentials = userDataService.getCredentials(httpServletRequest);

        if (credentials == null) {
            return "<div class=\"table-scroll\"><p>Please log in to see recommendations.</p></div>";
        }

        PersistentUserData userData = userDataService.getServerUserData(credentials);

        if (userData.getLinkedOsuId() == null) {
            return "<div class=\"table-scroll\"><p>Please link your osu! account to see recommendations.</p></div>";
        }

        List<Beatmap> recommendations = recommendationsService.getRecommendations(credentials);

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"table-scroll\">");
        html.append(BeatmapTableResource.formatBeatmapsTableHeader(userData.getSettings(), false, null, 0));
        html.append("<tbody>");
        for (Beatmap beatmap : recommendations) {
            formatRecommendationsRow(beatmap, userData, html);
        }
        html.append("</tbody></table></div>");
        html.append(BeatmapTableResource.formatEditDialog());
        return html.toString();
    }

    @POST
    @Path("/hide")
    @Produces(MediaType.TEXT_HTML)
    public String hideRecommendation(
            @Context HttpServletRequest httpServletRequest,
            @FormParam("beatmapid") int beatmapid,
            @FormParam("mods") @CheckForNull String mods)
            throws PpaddictException {
        Credentials credentials = userDataService.getCredentialsOrThrow(httpServletRequest);

        Beatmap beatmap = recommendationsService.hideRecommendation(credentials, beatmapid, mods);

        StringBuilder html = new StringBuilder();
        formatRecommendationsRow(beatmap, userDataService.getServerUserData(credentials), html);
        return html.toString();
    }

    private static void formatRecommendationsRow(Beatmap beatmap, PersistentUserData userData, StringBuilder html) {
        String modsEscaped = beatmap.mods != null ? beatmap.mods.replace("\"", "\\\"") : "";
        BeatmapTableResource.formatBeatmapTablesRow(
                beatmap,
                html,
                true,
                userData.getSettings().isOpenDirectOnMapSelect(),
                HIDE_BUTTON_TEMPLATE.formatted(beatmap.beatmapid, modsEscaped));
    }
}

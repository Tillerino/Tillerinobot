package org.tillerino.ppaddict.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;

@Path("/user/settings")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SettingsResource {

    private final UserDataServiceImpl userDataService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getSettings(@Context HttpServletRequest request) throws PpaddictException {
        Credentials credentials = userDataService.getCredentialsOrThrow(request);
        PersistentUserData userData = userDataService.getServerUserData(credentials);
        Settings settings = userData.getSettings();
        return renderSettingsDialog(settings);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public String saveSettings(
            @Context HttpServletRequest request,
            @FormParam("openDirectOnMapSelect") boolean openDirectOnMapSelect,
            @FormParam("applyOtherFiltersWithTextFilter") boolean applyOtherFiltersWithTextFilter,
            @FormParam("recommendationsParameters") String recommendationsParameters,
            @FormParam("lowAccuracy") String lowAccuracyStr,
            @FormParam("highAccuracy") String highAccuracyStr)
            throws PpaddictException {
        Credentials credentials = userDataService.getCredentialsOrThrow(request);

        Settings settings = new Settings();
        settings.setOpenDirectOnMapSelect(openDirectOnMapSelect);
        settings.setApplyOtherFiltersWithTextFilter(applyOtherFiltersWithTextFilter);

        if (recommendationsParameters != null && !recommendationsParameters.equals("*")) {
            settings.setRecommendationsParameters(recommendationsParameters);
        }

        double lowAcc = 93;
        double highAcc = 100;
        try {
            if (!StringUtils.isBlank(lowAccuracyStr)) {
                lowAcc = Double.parseDouble(lowAccuracyStr);
            }
            if (!StringUtils.isBlank(highAccuracyStr)) {
                highAcc = Double.parseDouble(highAccuracyStr);
            }
        } catch (NumberFormatException e) {
            return """
                    <div style="color: red;">Invalid accuracy value.</div>
                    """;
        }
        settings.setLowAccuracy(lowAcc);
        settings.setHighAccuracy(highAcc);

        try {
            userDataService.saveSettings(settings, credentials);
        } catch (PpaddictException e) {
            return """
                    <div style="color: red;">%s</div>""".formatted(e.getMessage());
        }

        return """
                <div style="color: green;">Saved.</div>""";
    }

    private String renderSettingsDialog(Settings settings) {
        String openDirect = settings.isOpenDirectOnMapSelect() ? "checked" : "";
        String otherFilters = settings.isApplyOtherFiltersWithTextFilter() ? "checked" : "";
        String recParams =
                settings.getRecommendationsParameters() == null ? "*" : settings.getRecommendationsParameters();
        String lowAcc = String.valueOf((int) settings.getLowAccuracy());
        String highAcc = String.valueOf((int) settings.getHighAccuracy());

        return """
                <b>Settings</b>
                <form hx-post="/htmx/user/settings" hx-target="#settings-save-result">
                  <p><input type="checkbox" name="openDirectOnMapSelect" %s> Open map in osu!direct when selected</p>
                  <p><input type="checkbox" name="applyOtherFiltersWithTextFilter" %s> Apply other filters when searching in name or notes</p>
                  <p>Recommendations settings:<br /><input type="text" name="recommendationsParameters" value="%s" /></p>
                  <p>Accuracy (%%) <input type="text" name="lowAccuracy" value="%s" style="width: 50px;" /> - <input type="text" name="highAccuracy" value="%s" style="width: 50px;" /></p>
                  <p id="settings-save-result"></p>
                  <button type="submit">Save</button>
                </form>
                """.formatted(openDirect, otherFilters, recParams, lowAcc, highAcc);
    }
}

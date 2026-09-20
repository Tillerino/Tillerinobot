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

        if (recommendationsParameters != null
                && !recommendationsParameters.isEmpty()
                && !recommendationsParameters.equals("*")) {
            settings.setRecommendationsParameters(recommendationsParameters);
        } else {
            settings.setRecommendationsParameters(null);
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

    @GET
    @Path("/apikey")
    @Produces(MediaType.TEXT_HTML)
    public String apiKeyDialog(@Context HttpServletRequest request) {
        return """
            <div id="apikey-confirm-content">
              <p>This will revoke any existing API key. Are you sure?</p>
              <button hx-post="/htmx/user/settings/apikey" hx-target="#apikey-confirm-content" hx-swap="innerHTML">Confirm</button>
            </div>""";
    }

    @POST
    @Path("/apikey")
    @Produces(MediaType.TEXT_HTML)
    public String createApiKey(@Context HttpServletRequest request) {
        try {
            String key = userDataService.createApiKey(userDataService.getCredentialsOrThrow(request));
            return """
                    Your new API key is:
                    <br />
                    <br />
                    <code style="word-break: break-all;">%s</code>
                    <br />
                    <br />
                    Make sure you save it - you won't be able to access it again.""".formatted(key);
        } catch (PpaddictException e) {
            return """
                    <p style="color: red;">%s</p>""".formatted(e.getMessage());
        }
    }

    private static final HelpEntry HELP_RECOMMENDATIONS_SETTINGS = new HelpEntry(
            "Recommendations settings",
            "You can customize your recommendations as if you were using Tillerinobot."
                    + " Put anything that you would send Tillerinobot after \"!recommend\" in this box. "
                    + "<a href=\"https://github.com/Tillerino/Tillerinobot/wiki/Recommendations\" target=\"_blank\">(more info)</a>"
                    + " Putting \"*\" will give you default recommendations, or reuse the last settings"
                    + " (including those from Tillerinobot), if you recently used any customizations.",
            "right-below");

    private String renderSettingsDialog(Settings settings) {
        String openDirect = settings.isOpenDirectOnMapSelect() ? "checked" : "";
        String otherFilters = settings.isApplyOtherFiltersWithTextFilter() ? "checked" : "";
        String recParams =
                settings.getRecommendationsParameters() == null ? "*" : settings.getRecommendationsParameters();
        String lowAcc = String.valueOf((int) settings.getLowAccuracy());
        String highAcc = String.valueOf((int) settings.getHighAccuracy());

        return """
                <p style="display: flex; justify-content: space-between;"><b>Settings</b>
                  <a href="#" class="help-trigger" onclick="showHelp()">Help</a></p>
                <form id="settings-form" hx-post="/htmx/user/settings" hx-target="#settings-save-result" hx-on::after-request="if (!event.detail.failed && event.detail.xhr.responseText.includes('Saved.')) { startCountdown(5, function() { window.location.reload(); }); }">
                  <p><input type="checkbox" name="openDirectOnMapSelect" value="true" %s> Open map in osu!direct when selected</p>
                  <p><input type="checkbox" name="applyOtherFiltersWithTextFilter" value="true" %s> Apply other filters when searching in name or notes</p>
                  <p><button hx-get="/htmx/user/settings/apikey" hx-target="#apikey-confirm-modal" hx-on::after-request="positionRelativeTo(document.getElementById('apikey-confirm-modal'), this)" command="show-modal" commandfor="apikey-confirm-modal">Create API key</button></p>
                  <p>Recommendations settings:<br /><input type="text" name="recommendationsParameters" value="%s"%s /></p>
                  <p>Accuracy (%%) <input type="text" name="lowAccuracy" value="%s" style="width: 50px;" /> - <input type="text" name="highAccuracy" value="%s" style="width: 50px;" /></p>
                  <p id="settings-save-result"></p>
                  <button type="submit">Save</button>
                </form>
                <dialog id="apikey-confirm-modal" class="modal" closedby="any" top="bottom" left="left" />
                """.formatted(openDirect, otherFilters, recParams, HELP_RECOMMENDATIONS_SETTINGS, lowAcc, highAcc);
    }
}

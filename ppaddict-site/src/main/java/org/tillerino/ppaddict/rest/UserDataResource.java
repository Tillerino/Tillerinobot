package org.tillerino.ppaddict.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.AuthLeaveService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.PpaddictException;

@Path("/user")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserDataResource {

    private final UserDataServiceImpl userDataService;
    private final AuthLeaveService authLeaveService;
    private final @AuthenticatorServices List<AuthenticatorService> authServices;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getUserArea(@Context HttpServletRequest request) throws PpaddictException {
        Credentials credentials = userDataService.getCredentials(request);
        String referer = request.getHeader("referer");

        if (credentials == null) {
            return renderNotLoggedIn(referer);
        }

        UserDataServiceImpl.UserData userData = userDataService.createUserData(request, credentials);
        return renderLoggedIn(userData.getClientUserData().nickname, userData.getClientUserData().logoutURL);
    }

    private static final HelpEntry HELP_LOGIN_BUTTON = new HelpEntry(
            "login",
            "You can login with your osu! account. This will enable several customizations and recommendations.",
            "below-left");
    private static final HelpEntry HELP_LOGIN_DIALOG = new HelpEntry(
            "You can log in using the provider above."
                    + " None of your data on this side should make it to the provider"
                    + " and ppaddict will only look for a way to identify you.",
            "below-right");

    private String renderNotLoggedIn(String referer) {
        String links = authServices.stream()
                .map(s -> {
                    String url = authLeaveService.getURL(s.getIdentifier(), referer);
                    return """
                            <br /><a href="%s">%s</a>
                            """.formatted(url, s.getDisplayName());
                })
                .collect(Collectors.joining(""));

        return """
                <button id="login-btn"%s command="show-modal" commandfor="login-modal">Login</button>
                <dialog id="login-modal" class="modal" closedby="any" top="bottom" right="right"%s>
                  <b>Choose an identity provider</b>
                  <br />
                  %s
                </dialog>
                """.formatted(HELP_LOGIN_BUTTON, HELP_LOGIN_DIALOG, links);
    }

    private static String renderLoggedIn(String displayName, String logoutURL) {
        return """
                <span class="username">%s</span>
                <button id="settings-btn" hx-get="/htmx/user/settings" hx-target="#settings-modal" hx-swap="innerHTML" hx-trigger="click" hx-on::after-request="this.nextElementSibling.showModal(); positionRelativeTo(this.nextElementSibling, this)">Settings</button>
                <dialog id="settings-modal" class="modal" top="bottom" right="right"></dialog>
                <a href="%s" class="chilla">Logout</a>
                """.formatted(displayName, logoutURL);
    }

    @POST
    @Path("/comment")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String saveComment(
            @Context HttpServletRequest request,
            @FormParam("beatmapid") int beatmapid,
            @FormParam("mods") String mods,
            @FormParam("comment") String comment)
            throws PpaddictException {
        userDataService.getCredentialsOrThrow(request);
        userDataService.saveComment(request, beatmapid, mods, comment);
        String m = mods == null ? "" : mods;
        String selector = "div.comments[data-beatmapid='%d'][data-mods='%s']".formatted(beatmapid, m);
        String text = comment.trim();
        return "<div class=\"comments\" data-beatmapid=\"%d\" data-mods=\"%s\" hx-swap-oob=\"outerHTML:%s\">%s</div>"
                .formatted(
                        beatmapid,
                        m,
                        selector,
                        text.isEmpty() ? "" : text + "<span class=\"commentsdate\">a moment ago</span>");
    }
}

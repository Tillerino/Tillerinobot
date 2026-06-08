package org.tillerino.ppaddict.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
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

        return credentials == null ? renderNotLoggedIn(referer) : renderLoggedIn(credentials, referer);
    }

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
                <button id="login-btn" command="show-modal" commandfor="login-modal">Login</button>
                <dialog id="login-modal" class="modal" closedby="any" top="bottom" right="right">
                  <b>Choose an identity provider</b>
                  <br />
                  %s
                </dialog>
                """.formatted(links);
    }

    private String renderLoggedIn(Credentials credentials, String referer) {
        String logoutUrl =
                "/authlogout?returnTo=" + java.net.URLEncoder.encode(referer, java.nio.charset.StandardCharsets.UTF_8);
        return """
                <span class="username">%s</span>
                <button id="settings-btn" hx-get="/htmx/user/settings" hx-target="#settings-modal" hx-swap="innerHTML" hx-trigger="click" hx-on::after-request="this.nextElementSibling.showModal(); positionRelativeTo(this.nextElementSibling, this)">Settings</button>
                <dialog id="settings-modal" class="modal" closedby="any" top="bottom" right="right"></dialog>
                <a href="%s" class="chilla">Logout</a>
                """.formatted(credentials.displayName, logoutUrl);
    }
}

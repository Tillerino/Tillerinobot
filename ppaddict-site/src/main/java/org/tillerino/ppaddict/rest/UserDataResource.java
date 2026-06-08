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
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.AuthLeaveService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.Credentials;

@Path("/user")
public class UserDataResource {

    private final UserDataServiceImpl userDataService;
    private final AuthLeaveService authLeaveService;
    private final List<AuthenticatorService> authServices;

    @Inject
    public UserDataResource(
            UserDataServiceImpl userDataService,
            AuthLeaveService authLeaveService,
            @AuthenticatorServices List<AuthenticatorService> authServices) {
        this.userDataService = userDataService;
        this.authLeaveService = authLeaveService;
        this.authServices = authServices;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getUserArea(@Context HttpServletRequest request)
            throws org.tillerino.ppaddict.shared.PpaddictException {
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
                <button id="settings-btn" command="show-modal" commandfor="settings-modal">Settings</button>
                <a href="%s" class="chilla">Logout</a>
                <dialog id="settings-modal" class="modal" closedby="any" top="bottom" right="right">
                  <b>Settings</b>
                  <br />
                  <br />
                  Coming soon...
                </dialog>
                """.formatted(credentials.displayName, logoutUrl);
    }
}

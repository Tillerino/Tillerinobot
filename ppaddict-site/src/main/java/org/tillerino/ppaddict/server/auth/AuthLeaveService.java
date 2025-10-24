package org.tillerino.ppaddict.server.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.scribe.model.Token;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.implementations.OauthServiceIdentifier;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthLeaveService extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String PATH = "/authleave";

    private final @AuthenticatorServices List<AuthenticatorService> services;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String finalReturnUrl = req.getParameter("returnTo");

        String serviceKey = req.getParameter("service");

        Optional<AuthenticatorService> authService = services.stream()
                .filter(s -> s.getIdentifier().equals(serviceKey))
                .findAny();
        if (authService.isEmpty()) {
            // send 400, its a user error, serviceKey is user-input
            resp.sendError(400, "Could not find service.");
            return;
        }

        OAuthService service = authService.get().getService();

        req.getSession().setAttribute(AuthArriveService.AUTH_RETURN_TO_SESSION_KEY, finalReturnUrl);
        req.getSession().setAttribute(AuthArriveService.AUTH_SERVICE_SESSION_KEY, serviceKey);

        Token token = null;
        if (service instanceof OAuth10aServiceImpl) {
            token = service.getRequestToken();
        }
        req.getSession().setAttribute(AuthArriveService.AUTH_REQUEST_TOKEN_SESSION_KEY, token);
        resp.sendRedirect(service.getAuthorizationUrl(token));
    }

    public String getURL(@OauthServiceIdentifier String serviceIdentifier, String returnTo) {
        return PATH + "?service=" + serviceIdentifier + "&returnTo="
                + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
    }
}

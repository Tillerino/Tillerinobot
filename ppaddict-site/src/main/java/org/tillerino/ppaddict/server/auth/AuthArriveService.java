package org.tillerino.ppaddict.server.auth;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.scribe.model.Token;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.tillerino.ppaddict.server.UserDataServiceImpl;

@Singleton
@Slf4j
public class AuthArriveService extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String PATH = "/autharrive";

    @Inject
    @AuthenticatorServices
    List<AuthenticatorService> services;

    @Inject
    UserDataServiceImpl userDataService;

    static final String AUTH_RETURN_TO_SESSION_KEY = "ppaddict.auth.returnTo";
    static final String AUTH_REQUEST_TOKEN_SESSION_KEY = "ppaddict.auth.requestToken";
    static final String AUTH_SERVICE_SESSION_KEY = "ppaddict.auth.service";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String returnTo = (String) req.getSession().getAttribute(AUTH_RETURN_TO_SESSION_KEY);
        Token requestToken = (Token) req.getSession().getAttribute(AUTH_REQUEST_TOKEN_SESSION_KEY);
        String serviceKey = (String) req.getSession().getAttribute(AUTH_SERVICE_SESSION_KEY);

        req.getSession().removeAttribute(AUTH_RETURN_TO_SESSION_KEY);
        req.getSession().removeAttribute(AUTH_REQUEST_TOKEN_SESSION_KEY);
        req.getSession().removeAttribute(AUTH_SERVICE_SESSION_KEY);

        Optional<AuthenticatorService> serviceMaybe = services.stream()
                .filter(s -> s.getIdentifier().equals(serviceKey))
                .findAny();
        if (serviceMaybe.isEmpty()) {
            resp.sendError(400, "Could not find service.");
            return;
        }
        AuthenticatorService service = serviceMaybe.get();

        if (service instanceof OAuth10aServiceImpl && requestToken == null) {
            resp.sendError(400, "Missing session. ");
            return;
        }

        try {
            Credentials credentials = service.createUser(req, requestToken);
            userDataService.rememberCredentials(req, resp, credentials);
        } catch (Exception e) {
            log.error("Error while processing auth token", e);
            resp.sendError(500);
            return;
        }

        resp.sendRedirect(StringUtils.defaultString(returnTo, "/"));
    }
}

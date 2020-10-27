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

import org.scribe.model.Token;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.shared.PpaddictException;

@Singleton
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
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String returnTo = (String) req.getSession().getAttribute(AUTH_RETURN_TO_SESSION_KEY);
    Token requestToken = (Token) req.getSession().getAttribute(AUTH_REQUEST_TOKEN_SESSION_KEY);
    String serviceKey = (String) req.getSession().getAttribute(AUTH_SERVICE_SESSION_KEY);

    req.getSession().removeAttribute(AUTH_RETURN_TO_SESSION_KEY);
    req.getSession().removeAttribute(AUTH_REQUEST_TOKEN_SESSION_KEY);
    req.getSession().removeAttribute(AUTH_SERVICE_SESSION_KEY);

    Optional<AuthenticatorService> service = services.stream().filter(s -> s.getIdentifier().equals(serviceKey)).findAny();
    if(service.isEmpty()) {
      resp.sendError(500);
      return;
    }

    Credentials credentials = service.get().createUser(req, requestToken);

    try {
      userDataService.rememberCredentials(req, resp, credentials);
    } catch (PpaddictException e) {
      resp.sendError(500);
      return;
    }

    resp.sendRedirect(returnTo);
  }
}

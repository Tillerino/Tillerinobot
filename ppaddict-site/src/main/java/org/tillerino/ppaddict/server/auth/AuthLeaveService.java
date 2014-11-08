package org.tillerino.ppaddict.server.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.model.Token;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.scribe.oauth.OAuthService;

@Singleton
public class AuthLeaveService extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public static final String PATH = "/authleave";

  @Inject
  @AuthenticatorServices
  Map<String, AuthenticatorService> services;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String finalReturnUrl = req.getParameter("returnTo");

    String serviceKey = req.getParameter("service");

    AuthenticatorService authService = services.get(serviceKey);

    OAuthService service = authService.getService();

    req.getSession().setAttribute(AuthArriveService.AUTH_RETURN_TO_SESSION_KEY, finalReturnUrl);
    req.getSession().setAttribute(AuthArriveService.AUTH_SERVICE_SESSION_KEY, serviceKey);

    Token token = null;
    if (service instanceof OAuth10aServiceImpl) {
      token = service.getRequestToken();
    }
    req.getSession().setAttribute(AuthArriveService.AUTH_REQUEST_TOKEN_SESSION_KEY, token);
    resp.sendRedirect(service.getAuthorizationUrl(token));
  }

  public String getURL(String service, String returnTo) {
    try {
      return PATH + "?service=" + service + "&returnTo=" + URLEncoder.encode(returnTo, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}

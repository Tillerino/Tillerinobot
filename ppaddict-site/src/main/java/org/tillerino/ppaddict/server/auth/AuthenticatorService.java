package org.tillerino.ppaddict.server.auth;

import javax.servlet.http.HttpServletRequest;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

public interface AuthenticatorService {
  OAuthService getService();

  Credentials createUser(OAuthService service, HttpServletRequest req, Token requestToken);
}

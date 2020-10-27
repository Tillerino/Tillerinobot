package org.tillerino.ppaddict.server.auth;

import javax.servlet.http.HttpServletRequest;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.implementations.OauthServiceIdentifier;

public interface AuthenticatorService {
  /**
   * @return something that uniquely identifiers this AuthenticatorService. It will be used as a get-param, so no fancy characters
   */
  @OauthServiceIdentifier
  String getIdentifier();

  /**
   * @return How this AuthenticatorService is called in the UI.
   */
  String getDisplayName();

  OAuthService getService();

  Credentials createUser(HttpServletRequest req, Token requestToken);
}

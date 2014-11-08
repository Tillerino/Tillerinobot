package org.tillerino.ppaddict.auth;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class FakeAuthenticatorService implements AuthenticatorService {

  @Override
  public OAuthService getService() {
    OAuthService myService = mock(OAuthService.class);
    when(myService.getAuthorizationUrl(any(Token.class))).thenReturn(FakeAuthenticatorWebsite.PATH);
    return myService;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(OAuthService service, HttpServletRequest req, Token requestToken) {
    Credentials credentials =
        new Credentials("local:" + req.getParameter("username"), req.getParameter("username"));
    return credentials;
  }

}

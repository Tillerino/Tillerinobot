package org.tillerino.ppaddict.auth;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.AbstractAuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class FakeAuthenticatorService extends AbstractAuthenticatorService {

  public FakeAuthenticatorService() {
    super("local", "Local", getMockedService());
  }

  private static OAuthService getMockedService() {
    OAuthService myService = mock(OAuthService.class);
    when(myService.getAuthorizationUrl(argThat(x -> true /* match any including null */)))
        .thenReturn(FakeAuthenticatorWebsite.PATH);
    return myService;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(HttpServletRequest req, Token requestToken) {
    Credentials credentials =
        new Credentials(getIdentifier() + ":" + req.getParameter("username"), req.getParameter("username"));
    return credentials;
  }

}

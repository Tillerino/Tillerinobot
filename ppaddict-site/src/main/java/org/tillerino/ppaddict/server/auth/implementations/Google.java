package org.tillerino.ppaddict.server.auth.implementations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Google2Api;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Singleton
public class Google implements AuthenticatorService {
  final OAuthService service;

  @Inject
  public Google(@Named("ppaddict.auth.returnURL") String returnURL,
      @Named("ppaddict.auth.google.clientId") String apiKey,
      @Named("ppaddict.auth.google.clientSecret") String apiSecret) {
    service =
        new ServiceBuilder().provider(Google2Api.class).apiKey(apiKey).apiSecret(apiSecret)
            .scope("email").callback(returnURL).build();
  }

  @Override
  public OAuthService getService() {
    return service;
  }

  Gson gson = new Gson();

  public static class GoogleUserinfoBody {
    String email;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(OAuthService service, HttpServletRequest req, Token requestToken) {
    Verifier verifier = new Verifier(req.getParameter(OAuthConstants.CODE));

    Token accessToken = service.getAccessToken(requestToken, verifier);

    OAuthRequest request =
        new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v2/userinfo");

    service.signRequest(accessToken, request);

    Response response = request.send();

    GoogleUserinfoBody fromJson = gson.fromJson(response.getBody(), GoogleUserinfoBody.class);

    Credentials user = new Credentials("google:" + fromJson.email, fromJson.email);

    return user;
  }
}

package org.tillerino.ppaddict.server.auth.implementations;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Google2Api;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.server.auth.CredentialsWithOsu;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class OsuOauth implements AuthenticatorService {
  final OAuthService service;

  @Inject
  public OsuOauth(@Named("ppaddict.auth.returnURL") String returnURL,
                  @Named("ppaddict.auth.osu.clientId") String clientId,
                  @Named("ppaddict.auth.osu.clientSecret") String clientSecret) {
    service =
        new ServiceBuilder().provider(OsuOauthApi.class).apiKey(clientId).apiSecret(clientSecret)
            .scope("identify").callback(returnURL).build();
  }

  @Override
  public String getIdentifier() {
    return "osu";
  }

  @Override
  public String getDisplayName() {
    return "osu!";
  }

  @Override
  public OAuthService getService() {
    return service;
  }

  Gson gson = new Gson();

  public static class OsuUserinfoBody {
    int id;
    String username;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(HttpServletRequest req, Token requestToken) {
    Verifier verifier = new Verifier(req.getParameter(OAuthConstants.CODE));

    Token accessToken = service.getAccessToken(requestToken, verifier);

    OAuthRequest request =
        new OAuthRequest(Verb.GET, "https://osu.ppy.sh/api/v2/me");

    service.signRequest(accessToken, request);

    Response response = request.send();

    OsuUserinfoBody fromJson = gson.fromJson(response.getBody(), OsuUserinfoBody.class);

    return new CredentialsWithOsu(fromJson.id, fromJson.username);
  }
}

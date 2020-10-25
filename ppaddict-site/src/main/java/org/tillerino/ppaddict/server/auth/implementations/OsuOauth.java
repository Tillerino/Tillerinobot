package org.tillerino.ppaddict.server.auth.implementations;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.tillerino.ppaddict.server.auth.AbstractAuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.server.auth.CredentialsWithOsu;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class OsuOauth extends AbstractAuthenticatorService {
  public static final String OSU_AUTH_SERVICE_IDENTIFIER = "osu-oauth";

  @Inject
  public OsuOauth(@Named("ppaddict.auth.returnURL") String returnURL,
                  @Named("ppaddict.auth.osu.clientId") String clientId,
                  @Named("ppaddict.auth.osu.clientSecret") String clientSecret) {
    super("osu-oauth", "osu!", new ServiceBuilder()
            .provider(OsuOauthApi.class)
            .apiKey(clientId)
            .apiSecret(clientSecret)
            .scope("identify")
            .callback(returnURL)
            .build());
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

    Token accessToken = getService().getAccessToken(requestToken, verifier);

    OAuthRequest request =
        new OAuthRequest(Verb.GET, "https://osu.ppy.sh/api/v2/me");

    getService().signRequest(accessToken, request);

    Response response = request.send();

    OsuUserinfoBody fromJson = gson.fromJson(response.getBody(), OsuUserinfoBody.class);

    return new CredentialsWithOsu(getIdentifier() + ":" + fromJson.id, fromJson.username, fromJson.id);
  }
}

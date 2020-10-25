package org.tillerino.ppaddict.server.auth.implementations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.tillerino.ppaddict.server.auth.AbstractAuthenticatorService;
import org.tillerino.ppaddict.server.auth.Credentials;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Singleton
public class Twitter extends AbstractAuthenticatorService {
  @Inject
  public Twitter(@Named("ppaddict.auth.returnURL") String returnURL,
      @Named("ppaddict.auth.twitter.apiKey") String apiKey,
      @Named("ppaddict.auth.twitter.apiSecret") String apiSecret) {
    super("twitter", "Twitter", new ServiceBuilder()
            .provider(TwitterApi.SSL.class)
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .callback(returnURL)
            .build());
  }

  Gson gson = new Gson();

  public static class TwitterVerifyBody {
    long id;
    String screen_name;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(HttpServletRequest req, Token requestToken) {
    Verifier verifier = new Verifier(req.getParameter(OAuthConstants.VERIFIER));

    Token accessToken = getService().getAccessToken(requestToken, verifier);

    OAuthRequest request =
        new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");

    getService().signRequest(accessToken, request);

    Response response = request.send();

    TwitterVerifyBody fromJson = gson.fromJson(response.getBody(), TwitterVerifyBody.class);

    Credentials user = new Credentials(getIdentifier() + ":" + fromJson.id, "@" + fromJson.screen_name);

    return user;
  }
}

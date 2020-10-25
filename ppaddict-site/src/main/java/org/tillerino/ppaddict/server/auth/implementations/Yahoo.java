package org.tillerino.ppaddict.server.auth.implementations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
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

public class Yahoo extends AbstractAuthenticatorService {
  @Inject
  public Yahoo(@Named("ppaddict.auth.returnURL") String returnURL,
      @Named("ppaddict.auth.yahoo.consumerKey") String apiKey,
      @Named("ppaddict.auth.yahoo.consumerSecret") String apiSecret) {
    super("yahoo", "Yahoo!", new ServiceBuilder()
            .provider(YahooApi.class)
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .callback(returnURL)
            .build());
  }

  Gson gson = new Gson();

  public static class YahooTinyProfile {
    String nickname;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(HttpServletRequest req, Token requestToken) {
    Verifier verifier = new Verifier(req.getParameter(OAuthConstants.CODE));

    Token accessToken = getService().getAccessToken(requestToken, verifier);

    OAuthRequest request = new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/me/guid");

    getService().signRequest(accessToken, request);

    Response response = request.send();

    String guid = response.getBody();

    request =
        new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/user/" + guid
            + "/profile/tinyusercard");

    YahooTinyProfile profile = gson.fromJson(request.send().getBody(), YahooTinyProfile.class);

    return new Credentials(guid, profile.nickname);
  }

}

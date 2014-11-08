package org.tillerino.ppaddict.server.auth.implementations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
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
public class Facebook implements AuthenticatorService {
  final OAuthService service;

  @Inject
  public Facebook(@Named("ppaddict.auth.returnURL") String returnURL,
      @Named("ppaddict.auth.facebook.appId") String apiKey,
      @Named("ppaddict.auth.facebook.appSecret") String apiSecret) {
    service =
        new ServiceBuilder().provider(FacebookApi.class).apiKey(apiKey).apiSecret(apiSecret)
            .callback(returnURL).build();
  }

  @Override
  public OAuthService getService() {
    return service;
  }

  Gson gson = new Gson();

  public static class FacebookMeBody {
    String id;
    String first_name;
    String last_name;
  }

  @SuppressFBWarnings(value = "TQ", justification = "Producer")
  @Override
  public Credentials createUser(OAuthService service, HttpServletRequest req, Token requestToken) {
    Verifier verifier = new Verifier(req.getParameter(OAuthConstants.CODE));

    Token accessToken = service.getAccessToken(requestToken, verifier);

    OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");

    service.signRequest(accessToken, request);

    Response response = request.send();

    FacebookMeBody fromJson = gson.fromJson(response.getBody(), FacebookMeBody.class);

    Credentials user =
        new Credentials("facebook:" + fromJson.id, fromJson.first_name + " " + fromJson.last_name);

    return user;
  }
}

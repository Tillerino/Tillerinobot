package org.tillerino.ppaddict.server.auth.implementations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
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

@Singleton
public class OsuOauth extends AbstractAuthenticatorService {
    public static final String OSU_AUTH_SERVICE_IDENTIFIER = "osu-oauth";

    @Inject
    public OsuOauth(
            @Named("ppaddict.auth.returnURL") String returnURL,
            @Named("ppaddict.auth.osu.clientId") String clientId,
            @Named("ppaddict.auth.osu.clientSecret") String clientSecret) {
        super(
                OSU_AUTH_SERVICE_IDENTIFIER,
                "osu!",
                new ServiceBuilder()
                        .provider(OsuOauthApi.class)
                        .apiKey(clientId)
                        .apiSecret(clientSecret)
                        .scope("identify")
                        .callback(returnURL)
                        .build());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OsuUserinfoBody {
        public int id;
        public String username;
    }

    @SuppressFBWarnings(value = "TQ", justification = "Producer")
    @Override
    public Credentials createUser(HttpServletRequest req, Token requestToken) {
        Verifier verifier = new Verifier(req.getParameter(OAuthConstants.CODE));

        Token accessToken = getService().getAccessToken(requestToken, verifier);

        OAuthRequest request = new OAuthRequest(Verb.GET, "https://osu.ppy.sh/api/v2/me");

        getService().signRequest(accessToken, request);

        Response response = request.send();

        OsuUserinfoBody fromJson;
        try {
            fromJson = new ObjectMapper().readValue(response.getBody(), OsuUserinfoBody.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new CredentialsWithOsu(getIdentifier() + ":" + fromJson.id, fromJson.username, fromJson.id);
    }
}

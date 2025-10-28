package org.tillerino.ppaddict.server.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.implementations.OauthServiceIdentifier;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;

public interface AuthenticatorService {
    /**
     * @return something that uniquely identifiers this AuthenticatorService. It will be used as a get-param, so no
     *     fancy characters
     */
    @OauthServiceIdentifier
    String getIdentifier();

    /** @return How this AuthenticatorService is called in the UI. */
    String getDisplayName();

    OAuthService getService();

    Credentials createUser(HttpServletRequest req, Token requestToken);

    @dagger.Module
    interface Module {
        @dagger.Provides
        @Singleton
        @AuthenticatorServices
        static List<AuthenticatorService> getAuthServices(@Named("ppaddict.auth.returnURL") String returnUrl) {
            List<AuthenticatorService> services = new ArrayList<>();

            String oauthOsuClientId = System.getenv("PPADDICT_OAUTH_OSU_CLIENT_ID");
            String oauthOsuClientSecret = System.getenv("PPADDICT_OAUTH_OSU_CLIENT_SECRET");
            if (oauthOsuClientId != null && oauthOsuClientSecret != null) {
                services.add(new OsuOauth(returnUrl, oauthOsuClientId, oauthOsuClientSecret));
            }

            return services;
        }
    }
}

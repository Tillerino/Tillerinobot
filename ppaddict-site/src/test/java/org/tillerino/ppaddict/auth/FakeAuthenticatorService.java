package org.tillerino.ppaddict.auth;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.AbstractAuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.Credentials;

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
        return new Credentials(getIdentifier() + ":" + req.getParameter("username"), req.getParameter("username"));
    }

    @dagger.Module
    public interface Module {
        @dagger.Provides
        @Singleton
        @AuthenticatorServices
        static List<AuthenticatorService> authenticatorServices(@Named("ppaddict.auth.returnURL") String returnUrl) {
            ArrayList<AuthenticatorService> authenticatorServices =
                    new ArrayList<>(AuthenticatorService.Module.getAuthServices(returnUrl));
            authenticatorServices.add(new FakeAuthenticatorService());
            return authenticatorServices;
        }
    }
}

package org.tillerino.ppaddict;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.EntityManagerFilter;
import org.tillerino.ppaddict.server.PpaddictUserDataService;
import org.tillerino.ppaddict.server.RateLimiterSettingsFilter;
import org.tillerino.ppaddict.server.RecommendationsServiceImpl;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.AuthModule;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;

public class PpaddictModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/ppaddict/beatmaps").with(BeatmapTableServiceImpl.class);
    serve("/ppaddict/user").with(UserDataServiceImpl.class);
    serve("/ppaddict/recommendations").with(RecommendationsServiceImpl.class);
    filter("/ppaddict/*").through(EntityManagerFilter.class);
    filter("/ppaddict/*").through(RateLimiterSettingsFilter.class);
    install(new AuthModule());
    bind(new TypeLiteral<AbstractPpaddictUserDataService<?>>() { }).to(PpaddictUserDataService.class);
  }

  @Provides
  @Singleton
  @AuthenticatorServices
  public List<AuthenticatorService> getAuthServices(@Named("ppaddict.auth.returnURL") String returnUrl) {
    return createAuthServices(returnUrl);
  }

  protected List<AuthenticatorService> createAuthServices(String returnUrl) {
    List<AuthenticatorService> services = new ArrayList<>();

    String oauthOsuClientId = System.getenv("PPADDICT_OAUTH_OSU_CLIENT_ID");
    String oauthOsuClientSecret = System.getenv("PPADDICT_OAUTH_OSU_CLIENT_SECRET");
    if(oauthOsuClientId != null && oauthOsuClientSecret != null) {
      services.add(new OsuOauth(returnUrl, oauthOsuClientId, oauthOsuClientSecret));
    }

    return services;
  }
}

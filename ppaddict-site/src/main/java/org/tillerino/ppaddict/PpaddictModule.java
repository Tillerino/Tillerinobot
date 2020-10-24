package org.tillerino.ppaddict;

import java.util.LinkedHashMap;
import java.util.Map;

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
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;

import com.google.inject.Provides;
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
    bind(AbstractPpaddictUserDataService.class).to(PpaddictUserDataService.class);
  }

  @Provides
  @Singleton
  @AuthenticatorServices
  public Map<String, AuthenticatorService> getAuthServices(@Named("ppaddict.auth.returnURL") String returnUrl) {
    return createAuthServices(returnUrl);
  }

  protected Map<String, AuthenticatorService> createAuthServices(String returnUrl) {
    return new LinkedHashMap<>();
  }
}

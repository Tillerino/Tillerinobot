package org.tillerino.ppaddict;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.tillerino.ppaddict.auth.FakeAuthenticatorService;
import org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.auth.AuthArriveService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;

import tillerino.tillerinobot.BotBackend;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;


public class PpaddictTestModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(String.class).annotatedWith(Names.named("ppaddict.auth.returnURL")).toInstance(
        "http://localhost:8080" + AuthArriveService.PATH);

    bind(Boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend"))
        .toInstance(true);

    bind(PpaddictBackend.class).to(TestBackend.class).in(Singleton.class);

    tillerino.tillerinobot.TestBackend botBackend = new tillerino.tillerinobot.TestBackend(true);

    bind(BotBackend.class).toInstance(botBackend);
    bind(tillerino.tillerinobot.TestBackend.class).toInstance(botBackend);

    serve(FakeAuthenticatorWebsite.PATH).with(FakeAuthenticatorWebsite.class);

    serve("/showErrorPage").with(ProducesError.class);

    install(new PpaddictModule());
  }

  @Provides
  @Singleton
  @AuthenticatorServices
  public Map<String, AuthenticatorService> getAuthServices(FakeAuthenticatorService local) {
    Map<String, AuthenticatorService> map = new HashMap<>();

    map.put("local", local);

    return map;
  }
}

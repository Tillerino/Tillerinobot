package org.tillerino.ppaddict;

import java.util.Map;

import javax.inject.Singleton;

import org.tillerino.ppaddict.auth.FakeAuthenticatorService;
import org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.auth.AuthArriveService;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import tillerino.tillerinobot.LocalConsoleTillerinobot;


public class PpaddictTestModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(String.class).annotatedWith(Names.named("ppaddict.auth.returnURL")).toInstance(
        "http://localhost:8080" + AuthArriveService.PATH);

    bind(String.class).annotatedWith(Names.named("ppaddict.apiauth.key")).toInstance("ppaddict-app-key");

    bind(Boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend"))
        .toInstance(true);

    bind(PpaddictBackend.class).to(TestBackend.class).in(Singleton.class);

    install(new LocalConsoleTillerinobot() {
      @Override
      protected void installMore() {
        // don't call super
      }
    });

    serve(FakeAuthenticatorWebsite.PATH).with(FakeAuthenticatorWebsite.class);

    serve("/showErrorPage").with(ProducesError.class);

    install(new PpaddictModule() {
      @Override
      protected Map<String, AuthenticatorService> createAuthServices(String returnUrl) {
        Map<String, AuthenticatorService> services = super.createAuthServices(returnUrl);
        services.put("local", new FakeAuthenticatorService());
        return services;
      }
    });
  }
}

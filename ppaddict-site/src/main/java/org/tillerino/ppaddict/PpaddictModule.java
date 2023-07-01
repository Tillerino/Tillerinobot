package org.tillerino.ppaddict;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.FilterInfo;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.PpaddictContextFilter;
import org.tillerino.ppaddict.server.RecommendationsServiceImpl;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.AuthModule;
import org.tillerino.ppaddict.server.auth.AuthenticatorService;
import org.tillerino.ppaddict.server.auth.AuthenticatorServices;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;

public class PpaddictModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/ppaddict/beatmaps").with(BeatmapTableServiceImpl.class);
    serve("/ppaddict/user").with(UserDataServiceImpl.class);
    serve("/ppaddict/recommendations").with(RecommendationsServiceImpl.class);
    filter("/ppaddict/*").through(PpaddictContextFilter.class);
    install(new AuthModule());
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

  public static PathHandler createGuiceFilterPathHandler(
      Class<? extends GuiceServletContextListener> config) throws ServletException {
    DeploymentInfo servletBuilder = Servlets.deployment()
        .setClassLoader(PpaddictModule.class.getClassLoader()).setContextPath("/")
        .setDeploymentName("test.war").addFilter(new FilterInfo("guiceFilter", GuiceFilter.class))
        .addFilterUrlMapping("guiceFilter", "/*", DispatcherType.REQUEST)
        .addListener(Servlets.listener(config)).addWelcomePage("Ppaddict.html")
        .addErrorPage(new ErrorPage("/error.html")).setResourceManager(
            new ClassPathResourceManager(PpaddictModule.class.getClassLoader(), "static"));

    DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
    manager.deploy();

    return Handlers.path(Handlers.redirect("/")).addPrefixPath("/", manager.start());
  }
}

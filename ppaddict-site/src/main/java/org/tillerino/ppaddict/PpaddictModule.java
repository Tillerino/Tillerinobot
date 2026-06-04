package org.tillerino.ppaddict;

import dagger.Binds;
import io.undertow.Handlers;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.LoggingExceptionHandler;
import jakarta.servlet.ServletException;
import java.util.Map;
import java.util.function.Consumer;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.PpaddictBackendImpl;
import org.tillerino.ppaddict.server.PpaddictCredentials;
import org.tillerino.ppaddict.server.PpaddictCredentials$RepoImpl;

@dagger.Module
public interface PpaddictModule {
    @Binds
    PpaddictBackend ppaddictBackend(PpaddictBackendImpl ppaddictBackend);

    @dagger.Binds
    PpaddictCredentials.Repo credentialsRepo(PpaddictCredentials$RepoImpl impl);

    static PathHandler createFilterPathHandler(Consumer<DeploymentInfo> config) throws ServletException {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(PpaddictModule.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("test.war")
                .addWelcomePage("Ppaddict.html")
                .addErrorPage(new ErrorPage("/error.html"))
                .setExceptionHandler(new LoggingExceptionHandler(Map.of()))
                .setResourceManager(new ClassPathResourceManager(PpaddictModule.class.getClassLoader(), "static"));

        config.accept(servletBuilder);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        return Handlers.path(Handlers.redirect("/")).addPrefixPath("/", manager.start());
    }
}

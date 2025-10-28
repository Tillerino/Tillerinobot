package org.tillerino.ppaddict;

import io.undertow.Handlers;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import jakarta.servlet.ServletException;
import java.util.function.Consumer;

public class PpaddictModule {

    public static PathHandler createFilterPathHandler(Consumer<DeploymentInfo> config) throws ServletException {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(PpaddictModule.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("test.war")
                .addWelcomePage("Ppaddict.html")
                .addErrorPage(new ErrorPage("/error.html"))
                .setResourceManager(new ClassPathResourceManager(PpaddictModule.class.getClassLoader(), "static"));

        config.accept(servletBuilder);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        return Handlers.path(Handlers.redirect("/")).addPrefixPath("/", manager.start());
    }
}

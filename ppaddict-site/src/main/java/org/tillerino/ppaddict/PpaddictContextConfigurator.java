package org.tillerino.ppaddict;

import io.undertow.servlet.api.*;
import jakarta.servlet.Servlet;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.tillerino.ppaddict.server.BeatmapTableServiceImpl;
import org.tillerino.ppaddict.server.PpaddictContextFilter;
import org.tillerino.ppaddict.server.RecommendationsServiceImpl;
import org.tillerino.ppaddict.server.UserDataServiceImpl;
import org.tillerino.ppaddict.server.auth.AuthArriveService;
import org.tillerino.ppaddict.server.auth.AuthLeaveService;
import org.tillerino.ppaddict.server.auth.AuthLogoutService;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PpaddictContextConfigurator {
    private final BeatmapTableServiceImpl beatmapTableService;
    private final UserDataServiceImpl userDataService;
    private final RecommendationsServiceImpl recommendationsService;

    private final AuthLeaveService authLeaveService;
    private final AuthArriveService authArriveService;
    private final AuthLogoutService authLogoutService;

    private final PpaddictContextFilter ppaddictContextFilter;

    public void configureUndertow(DeploymentInfo deploymentInfo) {
        addServlet(deploymentInfo, "/ppaddict/beatmaps", beatmapTableService);
        addServlet(deploymentInfo, "/ppaddict/user", userDataService);
        addServlet(deploymentInfo, "/ppaddict/recommendations", recommendationsService);

        addServlet(deploymentInfo, AuthLeaveService.PATH, authLeaveService);
        addServlet(deploymentInfo, AuthArriveService.PATH, authArriveService);
        addServlet(deploymentInfo, AuthLogoutService.PATH, authLogoutService);

        deploymentInfo.addFilter(new FilterInfo(
                "PpaddictContextFilter", PpaddictContextFilter.class, createInstanceFactory(ppaddictContextFilter)));
    }

    public static void addServlet(DeploymentInfo deploymentInfo, String mapping, Servlet servlet) {
        deploymentInfo.addServlet(
                new ServletInfo(servlet.getClass().getSimpleName(), servlet.getClass(), createInstanceFactory(servlet))
                        .addMapping(mapping));
    }

    public static <T> InstanceFactory<T> createInstanceFactory(T t) {
        return () -> new InstanceHandle<>() {
            @Override
            public T getInstance() {
                return t;
            }

            @Override
            public void release() {
                // ??
            }
        };
    }
}

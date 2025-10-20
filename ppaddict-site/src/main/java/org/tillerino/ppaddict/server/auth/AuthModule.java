package org.tillerino.ppaddict.server.auth;

import com.google.inject.servlet.ServletModule;

public class AuthModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve(AuthLeaveService.PATH).with(AuthLeaveService.class);
        serve(AuthArriveService.PATH).with(AuthArriveService.class);
        serve(AuthLogoutService.PATH).with(AuthLogoutService.class);
    }
}

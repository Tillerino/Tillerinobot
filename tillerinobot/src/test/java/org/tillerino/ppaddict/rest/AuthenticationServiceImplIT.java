package org.tillerino.ppaddict.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dagger.Component;
import jakarta.ws.rs.NotAuthorizedException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.MockServerRule;

public class AuthenticationServiceImplIT {
    @Singleton
    @Component(
            modules = {MockServerRule.MockServerModule.class, AuthenticationServiceImpl.RemoteAuthenticationModule.class
            })
    interface Injector {
        void inject(AuthenticationServiceImplIT t);
    }

    {
        DaggerAuthenticationServiceImplIT_Injector.create().inject(this);
    }

    @RegisterExtension
    public MockServerRule mockServer = new MockServerRule();

    @Inject
    AuthenticationService auth;

    @BeforeEach
    public void before() {
        mockServer.mockJsonGet("/auth/authorization", "{ \"unknownProperty\": true }", "api-key", "regular");
        mockServer.mockJsonGet("/auth/authorization", "{ \"admin\": true }", "api-key", "adminKey");
        mockServer.mockStatusCodeGet("/auth/authorization", 401, "api-key", "garbage");
    }

    @Test
    public void testPositive() throws Exception {
        assertThat(auth.getAuthorization("regular")).hasFieldOrPropertyWithValue("admin", false);
    }

    @Test
    public void testNegative() throws Exception {
        assertThatThrownBy(() -> auth.getAuthorization("garbage")).isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    public void testAdmin() throws Exception {
        assertThat(auth.getAuthorization("adminKey")).hasFieldOrPropertyWithValue("admin", true);
    }
}

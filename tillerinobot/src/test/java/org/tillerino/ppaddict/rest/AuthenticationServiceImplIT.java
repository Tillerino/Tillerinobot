package org.tillerino.ppaddict.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.TestBase;

public class AuthenticationServiceImplIT extends TestBase {
    @BeforeEach
    public void before() {
        wireMock.mockJsonGet("/auth/authorization", "{ \"unknownProperty\": true }", "api-key", "regular");
        wireMock.mockJsonGet("/auth/authorization", "{ \"admin\": true }", "api-key", "adminKey");
        wireMock.mockStatusCodeGet("/auth/authorization", 401, "api-key", "garbage");
    }

    @Test
    public void testPositive() {
        assertThat(auth.getAuthorization("regular")).hasFieldOrPropertyWithValue("admin", false);
    }

    @Test
    public void testNegative() {
        assertThatThrownBy(() -> auth.getAuthorization("garbage")).isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    public void testAdmin() {
        assertThat(auth.getAuthorization("adminKey")).hasFieldOrPropertyWithValue("admin", true);
    }
}

package org.tillerino.ppaddict.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;

import jakarta.ws.rs.NotAuthorizedException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.MockServerRule;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

@RunWith(InjectionRunner.class)
@TestModule({ MockServerRule.MockServerModule.class, AuthenticationServiceImpl.RemoteAuthenticationModule.class })
public class AuthenticationServiceImplIT{
	@Rule
	public MockServerRule mockServer = new MockServerRule();

	@Inject
	AuthenticationService auth;

	@Before
	public void before() {
		mockServer.mockJsonGet("/auth/authorization", "{ }", "api-key", "regular");
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

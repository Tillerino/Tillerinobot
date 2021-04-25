package org.tillerino.ppaddict.rest;

import javax.inject.Inject;
import javax.inject.Named;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import com.google.inject.AbstractModule;

/**
 * Implements the {@link AuthenticationService} against an internal HTTP API.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
	private final AuthenticationService remoteService;

	@Inject
	public AuthenticationServiceImpl(@Named("ppaddict.auth.url") String authServiceBaseUrl) {
		remoteService = WebResourceFactory.newResource(AuthenticationService.class,
				JerseyClientBuilder.createClient().target(authServiceBaseUrl));
	}

	@Override
	public Authorization getAuthorization(String key) {
		return remoteService.getAuthorization(key);
	}

	@Override
	public String createKey(String adminKey, int osuUserId) throws NotFoundException, ForbiddenException {
		return remoteService.createKey(adminKey, osuUserId);
	}

	public static class RemoteAuthenticationModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
		}
	}
}

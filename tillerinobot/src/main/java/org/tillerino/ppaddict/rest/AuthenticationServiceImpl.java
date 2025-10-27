package org.tillerino.ppaddict.rest;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.ResponseProcessingException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

/** Implements the {@link AuthenticationService} against an internal HTTP API. */
@Slf4j
@Singleton
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationService remoteService;

    @Inject
    public AuthenticationServiceImpl(@Named("ppaddict.auth.url") String authServiceBaseUrl) {
        remoteService = WebResourceFactory.newResource(
                AuthenticationService.class, JerseyClientBuilder.createClient().target(authServiceBaseUrl));
    }

    @Override
    public Authorization getAuthorization(String key) {
        try {
            return remoteService.getAuthorization(key);
        } catch (ResponseProcessingException e) {
            log.error("Error getting authorization", e);
            throw new InternalServerErrorException();
        }
    }

    @Override
    public String createKey(String adminKey, int osuUserId) throws NotFoundException, ForbiddenException {
        return remoteService.createKey(adminKey, osuUserId);
    }
}

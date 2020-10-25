package org.tillerino.ppaddict.server.auth;

import org.scribe.oauth.OAuthService;

public abstract class AbstractAuthenticatorService implements AuthenticatorService {
    private final String identifier;
    private final String displayName;
    private final OAuthService service;

    public AbstractAuthenticatorService(String identifier, String displayName, OAuthService service) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.service = service;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public OAuthService getService() {
        return service;
    }
}

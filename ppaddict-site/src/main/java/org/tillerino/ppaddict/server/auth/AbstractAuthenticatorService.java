package org.tillerino.ppaddict.server.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.implementations.OauthServiceIdentifier;

@RequiredArgsConstructor
@Getter
public abstract class AbstractAuthenticatorService implements AuthenticatorService {
    private final @OauthServiceIdentifier String identifier;
    private final String displayName;
    private final OAuthService service;
}

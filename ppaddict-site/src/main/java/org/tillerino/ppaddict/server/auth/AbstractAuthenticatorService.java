package org.tillerino.ppaddict.server.auth;

import org.scribe.oauth.OAuthService;
import org.tillerino.ppaddict.server.auth.implementations.OauthServiceIdentifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class AbstractAuthenticatorService implements AuthenticatorService {
  private final @OauthServiceIdentifier String identifier;
  private final String displayName;
  private final OAuthService service;
}

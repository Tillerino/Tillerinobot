package org.tillerino.ppaddict.server.auth;

import lombok.Getter;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;
import org.tillerino.ppaddict.web.types.PpaddictId;

/** Intermediate credentials returned by {@link OsuOauth} when logging in. */
public class CredentialsWithOsu extends Credentials {
    @Getter
    @UserId
    private final int osuUserId;

    public CredentialsWithOsu(@PpaddictId String identifier, String userName, @UserId int osuUserId) {
        super(identifier, userName);
        this.osuUserId = osuUserId;
    }
}

package org.tillerino.ppaddict.server.auth;

import org.tillerino.ppaddict.web.types.PpaddictId;

public class CredentialsWithOsu extends Credentials {
    public int osuUserId;

    public CredentialsWithOsu(@PpaddictId String identifier, String userName, int osuUserId) {
        super(identifier, userName);
        this.osuUserId = osuUserId;
    }
}

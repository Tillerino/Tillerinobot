package org.tillerino.ppaddict.server.auth;

public class CredentialsWithOsu extends Credentials {
    public static final String OSU_OAUTH_PREFIX = "osu-oauth:";
    public int osuUserId;

    public CredentialsWithOsu(int osuUserId, String userName) {
        super(OSU_OAUTH_PREFIX + osuUserId, userName);
        this.osuUserId = osuUserId;
    }
}

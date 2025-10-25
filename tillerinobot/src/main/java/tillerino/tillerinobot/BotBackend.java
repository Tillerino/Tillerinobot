package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;

public interface BotBackend {
    /**
     * @param nick
     * @return the last version of the bot that was visited by this user. -1 if no information available.
     * @throws SQLException
     * @throws UserException
     */
    int getLastVisitedVersion(@Nonnull @IRCName String nick) throws SQLException, UserException;

    void setLastVisitedVersion(@Nonnull @IRCName String nick, int version) throws SQLException;

    /**
     * Checks if a user is a donator/patron.
     *
     * @return a positive value if the user is a donator/patron.
     */
    int getDonator(@UserId int user) throws SQLException, IOException;

    /**
     * links the given user to a Patreon account using a token string.
     *
     * @param token a token that was emailed to the Patron.
     * @param user osu account to link to
     * @return the name of the Patreon account that current user was linked to, or null if the token was not valid
     */
    @CheckForNull
    String tryLinkToPatreon(String token, OsuApiUser user);
}

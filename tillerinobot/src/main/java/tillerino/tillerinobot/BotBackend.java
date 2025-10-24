package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
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
     * Registers activity of a user. This information is not historized, i.e. a single value is updated and the previous
     * value is therefore erased. This information is used to decide when to update top-scores of the users.
     *
     * <p>The implementation is reasonably fast and can be called frequently. It does not download anything from the API
     * and is unrelated to {@link #getUser(int, long)}.
     *
     * @param userid osu user ID of a real user. It is not required that the user is known via {@link #getUser(int,
     *     long)}.
     * @param timestamp when the user was sighted
     * @throws SQLException only on connection errors
     */
    void registerActivity(@UserId int userid, @MillisSinceEpoch long timestamp) throws SQLException;

    long getLastActivity(@Nonnull OsuApiUser user) throws SQLException;

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

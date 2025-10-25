package tillerino.tillerinobot;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;
import tillerino.tillerinobot.data.Player;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerService {
    private final DatabaseManager databaseManager;

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
    void registerActivity(@UserId int userid, @MillisSinceEpoch long timestamp) throws SQLException {
        try (Database database = databaseManager.getDatabase()) {
            Player.updateLastSeen(userid, database, timestamp);
        }
    }

    long getLastActivity(@Nonnull OsuApiUser user) throws SQLException {
        Player player;
        try (Database database = databaseManager.getDatabase()) {
            player = Player.getPlayer(database, user.getUserId());
        }
        return player.getLastseen();
    }
}

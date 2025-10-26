package tillerino.tillerinobot.data;

import dagger.Lazy;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.OsuApi;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PullThrough {
    private final DatabaseManager dbm;

    private final Lazy<OsuApi> downloader;

    /**
     * Get a user's information. If the user is not known in the database, the data will be downloaded from the osu API.
     *
     * @param userid user id
     * @param maxAge Maximum age of the information in milliseconds. If there is cached information in the database
     *     which is younger, the cached information will be returned. Otherwise, fresh data will be downloaded and
     *     cached. If <= 0, any cached information, if available, will be returned.
     * @return null if the user can't be found at the osu API
     * @throws IOException API exception
     */
    @CheckForNull
    public ApiUser getUser(@UserId int userid, long maxAge) throws SQLException, IOException {
        try (Database database = dbm.getDatabase()) {
            return ApiUser.loadOrDownload(database, userid, maxAge, this.downloader.get());
        }
    }

    @CheckForNull
    public ApiUser downloadUser(String userName) throws IOException, SQLException {
        ApiUser user;
        try (var _ = PhaseTimer.timeTask("downloadUser")) {
            System.out.println("downloading user " + userName);
            user = downloader.get().getUser(userName, GameModes.OSU);
            System.out.println(".downloaded user " + userName);
        }

        if (user != null) {
            try (Database database = dbm.getDatabase();
                    Persister<ApiUser> persisterApiUser = database.persister(ApiUser.class, Action.REPLACE)) {
                persisterApiUser.persist(user);
            }
        }
        return user;
    }

    /**
     * Retrieves the last plays from this user. These don't have pp and might be failed attempts.
     *
     * @return sorted from most recent to oldest
     */
    @Nonnull
    public List<ApiScore> getRecentPlays(@UserId int userid) throws IOException {
        try (var _ = PhaseTimer.timeTask("download recent")) {
            System.out.println("downloading recent plays for " + userid);
            List<ApiScore> recent = downloader.get().getUserRecent(userid, GameModes.OSU);
            System.out.println(".downloaded recent plays for " + userid);
            return recent;
        }
    }
}

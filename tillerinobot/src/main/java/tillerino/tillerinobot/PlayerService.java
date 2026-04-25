package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.data.Player;
import tillerino.tillerinobot.data.UserTop50Entry;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerService {
    private final DatabaseManager databaseManager;

    private final Player.Repo repo;

    private final ApiScore.Repo scoreRepo;

    private final UserTop50Entry.Repo top50EntryRepo;

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
        try (Database db = databaseManager.getDatabase()) {
            repo.updateLastSeen(db, userid, timestamp);
        }
    }

    long getLastActivity(@Nonnull ApiUser user) throws SQLException {
        Player player;
        try (Database db = databaseManager.getDatabase()) {
            player = getPlayer(db, user.getUserId());
        }
        return player.getLastseen();
    }

    public Player getPlayer(Database database, @UserId int userid) throws SQLException {
        {
            Optional<Player> player = repo.getPlayer(database, userid);
            if (player.isPresent()) return player.get();
        }
        Player player = new Player(userid);
        repo.insertPlayer(database, player);
        return player;
    }

    public Optional<Player> getPlayerForUpdate(Database d) throws SQLException {
        return repo.getPlayerForUpdate(d);
    }

    public void updateTop50(
            Database database, Player player, long maxAge, OsuApi downloader, Clock clock, ConfigService config)
            throws SQLException, IOException {
        if (player.getLastupdatetop50() > clock.currentTimeMillis() - maxAge) return;
        if (config.scoresMaintenance()) {
            throw new MaintenanceException("Scores maintenance");
        }

        List<ApiScore> scores;
        try (var _ = PhaseTimer.timeTask("downloadTop50")) {
            System.out.println("downloading top50 for " + player.getUserid());
            scores = downloader.getUserTop(player.getUserid(), GameModes.OSU, 50);
            System.out.println(".downloaded top50 for " + player.getUserid());
        }

        List<UserTop50Entry> top50Entries = new ArrayList<>(scores.size());
        for (int i = 0; i < scores.size(); i++) {
            ApiScore score = scores.get(i);

            UserTop50Entry top50Entry = new UserTop50Entry();
            top50Entry.beatmapid = score.getBeatmapId();
            top50Entry.userid = score.getUserId();
            top50Entry.place = i;

            top50Entries.add(top50Entry);
        }

        try (var _ = PhaseTimer.timeTask("persistTop50")) {
            scoreRepo.replaceAll(database, scores);
            top50EntryRepo.replaceAll(database, top50Entries);
        }

        player.setLastupdatetop50(clock.currentTimeMillis());
        player.setAgetop50(player.getLastseen() - player.getLastupdatetop50());
        repo.updateLastUpdate(database, player);
    }
}

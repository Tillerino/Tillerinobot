package tillerino.tillerinobot.data;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.OsuApi;

/** An osu! player who is not necessarily a bot user. */
@Data
@Table("players")
@KeyColumn("userid")
@NoArgsConstructor
public class Player {
    @UserId
    int userid;

    long lastseen;
    long lastupdatetop50;
    long agetop50;

    public static void updateLastSeen(int userid, Database conn, @MillisSinceEpoch long timestamp) throws SQLException {
        try (PreparedStatement statement = conn.prepare(
                "insert into players (userid, lastseen, agetop50) values (?, ?, ?)"
                        + " on duplicate key update lastseen = greatest(lastseen, values(lastseen)), agetop50 = greatest(lastseen, values(lastseen)) - lastupdatetop50")) {
            statement.setInt(1, userid);
            statement.setLong(2, timestamp);
            statement.setLong(3, timestamp);

            statement.executeUpdate();
        }
    }

    public void updateTop50(Database database, long maxAge, OsuApi downloader, Clock clock, ConfigService config)
            throws SQLException, IOException {
        if (lastupdatetop50 > clock.currentTimeMillis() - maxAge) return;
        if (config.scoresMaintenance()) {
            throw new MaintenanceException("Scores maintenance");
        }

        List<ApiScore> scores;
        try (var _ = PhaseTimer.timeTask("downloadTop50")) {
            System.out.println("downloading top50 for " + userid);
            scores = downloader.getUserTop(userid, GameModes.OSU, 50);
            System.out.println(".downloaded top50 for " + userid);
        }

        try (var _ = PhaseTimer.timeTask("persistTop50");
                Persister<ApiScore> scorePersister = database.persister(ApiScore.class, Action.REPLACE);
                Persister<UserTop50Entry> placePersister = database.persister(UserTop50Entry.class, Action.REPLACE)) {
            for (int i = 0; i < scores.size(); i++) {
                ApiScore score = scores.get(i);
                scorePersister.persist(score, scores.size());

                UserTop50Entry top50Entry = new UserTop50Entry();
                top50Entry.beatmapid = score.getBeatmapId();
                top50Entry.userid = score.getUserId();
                top50Entry.place = i;

                placePersister.persist(top50Entry, scores.size());
            }
        }

        try (var _ = PhaseTimer.timeTask("updatePlayer");
                PreparedStatement statement =
                        database.prepare("update players set lastupdatetop50 = ?, agetop50 = ? where userid = ?")) {
            statement.setLong(1, this.lastupdatetop50 = clock.currentTimeMillis());
            statement.setLong(2, this.agetop50 = this.lastseen - this.lastupdatetop50);
            statement.setInt(3, userid);
            statement.executeUpdate();
        }
    }

    public Player(@UserId int userid) {
        super();
        this.userid = userid;
    }

    public static Player getPlayer(Database database, @UserId int userid) throws SQLException {
        {
            Optional<Player> player = database.selectUnique(Player.class).execute("where userid = ", userid);
            if (player.isPresent()) return player.get();
        }
        Player player = new Player(userid);
        database.persist(player, Action.INSERT_IGNORE);
        return player;
    }
}

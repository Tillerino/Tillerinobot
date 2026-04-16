package tillerino.tillerinobot.data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.jagger.annotations.*;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;

/** An osu! player who is not necessarily a bot user. */
@Data
@NoArgsConstructor
@Table(name = "players")
public class Player {
    @UserId
    @Id
    int userid;

    long lastseen;
    long lastupdatetop50;
    long agetop50;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect
        List<Player> selectAll(Connection c) throws SQLException;

        @JdbcInsert("""
          insert into players (userid, lastseen, agetop50) values (:userid, :timestamp, :timestamp)
          on duplicate key update lastseen = greatest(lastseen, values(lastseen)), agetop50 = greatest(lastseen, values(lastseen)) - lastupdatetop50
          """)
        void updateLastSeen(Connection c, int userid, @MillisSinceEpoch long timestamp) throws SQLException;

        @JdbcUpdate(
                "update players set lastupdatetop50 = :player.lastupdatetop50, agetop50 = :player.agetop50 where userid = :player.userid")
        void updateLastUpdate(Connection c, Player player) throws SQLException;

        @JdbcSelect("select * from players where userid = :userid")
        Optional<Player> getPlayer(Connection c, int userid) throws SQLException;

        @JdbcInsert("""
          insert ignore into players (p.#c) values (:p.#v)""")
        void insertPlayer(Connection c, Player p) throws SQLException;

        @JdbcSelect("select * from players where agetop50 > 0 order by agetop50 desc limit 0, 1")
        Optional<Player> getPlayerForUpdate(Connection c) throws SQLException;
    }

    public Player(@UserId int userid) {
        super();
        this.userid = userid;
    }
}

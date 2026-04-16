package tillerino.tillerinobot.data;

import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.UserId;

@NoArgsConstructor
@AllArgsConstructor
@jakarta.persistence.Table(name = "usertop50")
public class UserTop50Entry {
    @UserId
    @jakarta.persistence.Id
    public int userid;

    @jakarta.persistence.Id
    public int place;

    @BeatmapId
    public int beatmapid;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert("replace into usertop50 (s.#columns) values (:s.#values)")
        void replaceAll(Connection c, Iterable<UserTop50Entry> s) throws SQLException;

        @JdbcInsert
        void insert(Connection c, UserTop50Entry entry) throws SQLException;
    }
}

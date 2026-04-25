package tillerino.tillerinobot.data;

import static jakarta.persistence.GenerationType.*;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JdbcUpdate;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

@Table(name = "givenrecommendations")
@Data
public class GivenRecommendation {
    public GivenRecommendation(@UserId int userid, @BeatmapId int beatmapid, long date, @BitwiseMods long mods) {
        super();
        this.userid = userid;
        this.beatmapid = beatmapid;
        this.date = date;
        this.mods = mods;
    }

    public GivenRecommendation() {}

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @UserId
    private int userid;

    @BeatmapId
    private int beatmapid;

    private long date;

    @BitwiseMods
    public long mods;

    /** If true, this won't be taken into consideration when generating recommendations. */
    private boolean forgotten = false;
    /** If true, this won't be displayed in the recommendations list in ppaddict anymore. */
    private boolean hidden = false;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert
        void insert(Connection c, GivenRecommendation r) throws SQLException;

        @JdbcSelect
        List<GivenRecommendation> getAll(Connection c) throws SQLException;

        @JdbcSelect(where = "`userid` = :userid and `date` > :cutoffDate and not forgotten order by `date` desc")
        List<GivenRecommendation> loadRecent(Connection c, @UserId int userid, long cutoffDate) throws SQLException;

        @JdbcSelect(where = "`userid` = :userid and not hidden order by `date` desc")
        List<GivenRecommendation> loadVisible(Connection c, @UserId int userid) throws SQLException;

        @JdbcUpdate(
                "update givenrecommendations set hidden = true where `userid` = :userId and `beatmapid` = :beatmapid and `mods` = :mods")
        void hide(Connection c, @UserId int userId, @BeatmapId int beatmapid, @BitwiseMods long mods)
                throws SQLException;

        @JdbcUpdate("update givenrecommendations set forgotten = true where userid = :userId")
        void forget(Connection connection, @UserId int userId) throws SQLException;
    }
}

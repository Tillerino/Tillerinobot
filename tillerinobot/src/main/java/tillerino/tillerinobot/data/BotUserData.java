package tillerino.tillerinobot.data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.types.UserId;

@Table(name = "userdata")
@Data
public class BotUserData {
    @UserId
    @Id
    private int userId;

    /*
     * At the time of this writing, the maximum length of data was 1440.
     */
    private String userdata;

    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    @JdbcConfig(quoteChar = "`")
    public interface Repo {
        @JdbcSelect(where = "`userId` = :userId")
        Optional<BotUserData> get(Connection c, @UserId int userId) throws SQLException;

        @JdbcInsert("""
                insert into `userdata` (`data.#insertColumns`) values (:data.#insertValues) \
                on duplicate key update `userdata` = :data.userdata""")
        void set(Connection c, BotUserData data) throws SQLException;
    }
}

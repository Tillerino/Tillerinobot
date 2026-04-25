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
import org.tillerino.jagger.annotations.JdbcUpdate;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;

@Table(name = "usernames")
@Data
public class UserNameMapping {
    @IRCName
    @Id
    private String userName;

    @UserId
    private int userid;

    private long resolved;

    private long firstresolveattempt;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect(where = "`userName` = :userName")
        Optional<UserNameMapping> findByUserName(Connection c, String userName) throws SQLException;

        @JdbcInsert("REPLACE INTO `usernames` (`mapping.#columns`) VALUES (:mapping.#values)")
        void replace(Connection c, UserNameMapping mapping) throws SQLException;

        @JdbcUpdate("DELETE FROM `usernames`")
        void deleteAll(Connection c) throws SQLException;
    }
}

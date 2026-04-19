package tillerino.tillerinobot.data;

import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;

@Table(name = "botusers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotUser {
    private String username;
    private int versionVisited;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect(where = "`username` = :username")
        Optional<BotUser> findByUsername(Connection c, String username) throws SQLException;

        @JdbcInsert("REPLACE INTO `botusers` (`b.#insertColumns`) VALUES (:b.#insertValues)")
        void replace(Connection c, BotUser b) throws SQLException;
    }
}

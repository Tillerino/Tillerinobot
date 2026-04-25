package tillerino.tillerinobot.data;

import jakarta.persistence.Id;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;

/** Configuration that can be changed while the bot runs. This is stored as simple key-value in the database. */
@Data
public class BotConfig {
    @Id
    private String path;

    private String value;

    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect("select * from botconfig where path = :path")
        Optional<BotConfig> get(Connection c, String path) throws SQLException;

        @JdbcInsert(
                "insert into botconfig (path, value) values (:config.path, :config.value) on duplicate key update value = :config.value")
        void set(Connection c, BotConfig config) throws SQLException;
    }
}

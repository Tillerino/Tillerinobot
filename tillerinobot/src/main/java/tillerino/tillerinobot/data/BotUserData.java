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
import org.tillerino.osuApiModel.types.UserId;

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
    public interface Repo {
        @JdbcSelect("select * from userdata where userId = :userId")
        Optional<BotUserData> get(Connection c, int userId) throws SQLException;

        @JdbcInsert(
                "insert into userdata (userId, userdata) values (:data.userId, :data.userdata) on duplicate key update userdata = :data.userdata")
        void set(Connection c, BotUserData data) throws SQLException;
    }
}

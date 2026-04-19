package org.tillerino.ppaddict.server;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.ppaddict.server.auth.Credentials;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "ppaddictcredentials")
public class PpaddictCredentials extends Credentials {
    private static final long serialVersionUID = 1L;

    @Id
    private String cookie;

    public PpaddictCredentials() {}

    public PpaddictCredentials(Credentials cred) {
        super(cred);
    }

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert
        void insert(Connection c, PpaddictCredentials pc) throws SQLException;

        @JdbcSelect(where = "`cookie` = :cookie and `expires` > :now")
        Optional<PpaddictCredentials> findByCookie(Connection c, String cookie, long now) throws SQLException;
    }

    public static synchronized String generateUniqueCookie(Connection c) throws SQLException {
        String cookie;
        try (PreparedStatement preparedStatement =
                c.prepareStatement("select * from `ppaddictcredentials` where `cookie` = ?")) {
            ResultSet set;
            do {
                cookie = LinkPpaddictHandler.newKey();
                preparedStatement.setString(1, cookie);
                set = preparedStatement.executeQuery();
            } while (set.next());
        }
        return cookie;
    }
}

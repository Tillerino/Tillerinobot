package org.tillerino.ppaddict.server;

import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.mormon.Table;
import org.tillerino.ppaddict.server.auth.Credentials;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("ppaddictcredentials")
public class PpaddictCredentials extends Credentials {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cookie;

    private PpaddictCredentials(Credentials cred) {
        super(cred);
    }

    public PpaddictCredentials() {}

    public static synchronized PpaddictCredentials createKey(Database database, Credentials userKey)
            throws SQLException {
        PpaddictCredentials pC = new PpaddictCredentials(userKey);

        try (PreparedStatement preparedStatement =
                database.prepare("select * from `ppaddictcredentials` where `cookie` = ?")) {
            ResultSet set;
            do {
                pC.cookie = LinkPpaddictHandler.newKey();
                preparedStatement.setString(1, pC.cookie);
                set = preparedStatement.executeQuery();
            } while (set.next());
        }

        database.persist(pC, Action.INSERT);

        return pC;
    }
}

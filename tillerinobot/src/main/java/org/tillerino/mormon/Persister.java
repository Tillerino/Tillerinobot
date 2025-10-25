package org.tillerino.mormon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 * Wraps a {@link PreparedStatement} to persist Java objects to the database. Depending on the chosen {@link Action},
 * this can be used to both insert and update rows.
 *
 * <p>This class will close the underlying {@link PreparedStatement} when closed. It implements {@link AutoCloseable},
 * so it is best used in a try-with block.
 */
public class Persister<T> implements AutoCloseable {
    public enum Action {
        INSERT("INSERT INTO"),
        INSERT_IGNORE("INSERT IGNORE INTO"),
        INSERT_DELAYED("INSERT DELAYED INTO"),
        REPLACE("REPLACE INTO"),
        ;
        private final String command;

        Action(String command) {
            this.command = command;
        }
    }

    private final PreparedStatement statement;

    private final Mapping<T> mapping;

    private int batched = 0;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    Persister(Database database, Class<T> cls, Action a) throws SQLException {
        mapping = Mapping.getOrCreateMapping(cls);

        statement = database.prepare(a.command + " `" + mapping.table() + "` (" + mapping.fields() + ") values ("
                + mapping.questionMarks() + ")");
    }

    public int persist(@Nonnull @NonNull T obj) throws SQLException {
        return persist(obj, 0);
    }

    public int persist(@Nonnull @NonNull T obj, int batchUpTo) throws SQLException {
        if (batched > 1 && batched >= batchUpTo) {
            statement.executeBatch();
            batched = 0;
        }
        mapping.set(obj, statement);
        if (batchUpTo <= 1) {
            return statement.executeUpdate();
        }
        statement.addBatch();
        batched++;
        if (batched >= batchUpTo) {
            statement.executeBatch();
            batched = 0;
        }
        return 0;
    }

    @Override
    public void close() throws SQLException {
        if (batched > 0) {
            statement.executeBatch();
            batched = 0;
        }
        statement.close();
    }
}

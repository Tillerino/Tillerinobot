package org.tillerino.mormon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper around a {@link Connection} which is used to create {@link Persister} and {@link Loader} instances along with
 * some other convenience methods.
 *
 * <p>This class will close the underlying {@link Connection} when closed. It implements {@link AutoCloseable}, so it is
 * best used in a try-with block. It is assumed that the connection comes from a pool and closing the connection
 * implementation will only return the actual connection back to the pool.
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public record Database(Connection connection) implements AutoCloseable {

    @Override
    public void close() throws SQLException {
        // We get a pooled connection, so this close just returns the connection to the pool.
        connection.close();
    }

    public interface UnpreparedStatement<T> {
        T execute(Object... query) throws SQLException;
    }
}

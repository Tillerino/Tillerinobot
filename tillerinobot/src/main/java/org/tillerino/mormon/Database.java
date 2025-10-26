package org.tillerino.mormon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.apache.commons.collections4.IteratorUtils;
import org.tillerino.mormon.Mapping.FieldManager;
import org.tillerino.mormon.Persister.Action;

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

    public PreparedStatement prepare(String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    private PreparedStatement prepareStatement(String prefix, Object... query) throws SQLException {
        List<String> literals = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (int i = 0; i < query.length; i++) {
            if (i % 2 == 0) {
                if (!(query[i] instanceof String s)) {
                    throw new IllegalArgumentException("Argument " + i + " not a string");
                }
                literals.add(s);
            } else {
                parameters.add(query[i]);
            }
        }

        String questionMarkQuery = prefix + " " + String.join("?", literals);
        if (literals.size() == parameters.size()) {
            questionMarkQuery += "?";
        }
        PreparedStatement ps = connection.prepareStatement(questionMarkQuery);

        try {
            int index = 1;
            for (Object value : parameters) {
                if (value == null) {
                    ps.setNull(index++, java.sql.Types.NULL);
                    continue;
                }
                if (value.getClass().isEnum()) {
                    value = ((Enum<?>) value).name();
                }
                switch (value) {
                    case Integer i -> ps.setInt(index++, i);
                    case Long l -> ps.setLong(index++, l);
                    case Float f -> ps.setFloat(index++, f);
                    case Double d -> ps.setDouble(index++, d);
                    case Boolean b -> ps.setBoolean(index++, b);
                    case String s -> ps.setString(index++, s);
                    default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass());
                }
            }
        } catch (Exception e) {
            try {
                ps.close();
            } catch (Exception ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }

        return ps;
    }

    public <T> UnpreparedStatement<List<T>> selectList(Class<T> cls) {
        Mapping<T> mapping = Mapping.getOrCreateMapping(cls);
        return st -> {
            try (PreparedStatement ps =
                    prepareStatement("select " + mapping.fields() + " from `" + mapping.table() + "`", st)) {
                ResultSet set = ps.executeQuery();
                return IteratorUtils.toList(new ResultSetIterator<>(set, mapping));
            }
        };
    }

    public <T> UnpreparedStatement<Optional<T>> selectUnique(Class<T> cls) {
        Mapping<T> mapping = Mapping.getOrCreateMapping(cls);
        return st -> {
            try (PreparedStatement ps =
                    prepareStatement("select " + mapping.fields() + " from `" + mapping.table() + "`", st)) {
                ResultSet set = ps.executeQuery();
                ResultSetIterator<T> iterator = new ResultSetIterator<>(set, mapping);
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                T obj = iterator.next();
                if (iterator.hasNext()) {
                    throw new SQLException("Result was not unique");
                }
                return Optional.of(obj);
            }
        };
    }

    /** @param query not null. query after table name. if you have conditions, start with "where". */
    public <T> Loader<T> loader(Class<? extends T> cls, String query) throws SQLException {
        return Loader.createLoader(this, cls, query, false);
    }

    /** @param query not null. query after table name. if you have conditions, start with "where". */
    public <T> Loader<T> streamingLoader(Class<? extends T> cls, String query) throws SQLException {
        return Loader.createLoader(this, cls, query, true);
    }

    public <T> Persister<T> persister(Class<T> cls, Action action) throws SQLException {
        return new Persister<>(this, cls, action);
    }

    /** Convenience method to persist a single object. */
    public <T> int persist(@Nonnull @NonNull T obj, Action a) throws SQLException {
        try (@SuppressWarnings("unchecked")
                Persister<T> persister = persister((Class<T>) obj.getClass(), a)) {
            return persister.persist(obj);
        }
    }

    public <T> UnpreparedStatement<Integer> deleteFrom(Class<T> cls) {
        Mapping<T> mapping = Mapping.getOrCreateMapping(cls);
        return st -> {
            try (PreparedStatement ps = prepareStatement("delete from `" + mapping.table() + "`", st)) {
                return ps.executeUpdate();
            }
        };
    }

    public <T> int truncate(Class<T> cls) throws SQLException {
        try (PreparedStatement s =
                prepare("truncate table `" + Mapping.getOrCreateMapping(cls).table() + "`")) {
            return s.executeUpdate();
        }
    }

    public <T> int delete(@NonNull T obj) throws SQLException {
        String query = Loader.getWhereQueryForKeyColumns(obj.getClass());
        Mapping<? extends Object> mapping = Mapping.getOrCreateMapping(obj.getClass());

        try (PreparedStatement s = prepare("delete from `" + mapping.table() + "` " + query)) {
            int parameterCount = s.getParameterMetaData().getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                FieldManager<?> fieldManager = mapping.fieldManagers().get(i);
                try {
                    fieldManager.toStatement(obj, s);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Error setting " + fieldManager.field(), e);
                }
            }

            return s.executeUpdate();
        }
    }

    public <T> long count(Class<T> cls, String query, Object... values) throws SQLException {
        try (PreparedStatement s = prepare(
                "select count(*) from `" + Mapping.getOrCreateMapping(cls).table() + "` " + query)) {
            Loader.setParameters(s, values);
            ResultSet set = s.executeQuery();
            set.next();
            return set.getLong(1);
        }
    }

    @Override
    public void close() throws SQLException {
        // We get a pooled connection, so this close just returns the connection to the pool.
        connection.close();
    }

    public interface UnpreparedStatement<T> {
        T execute(Object... query) throws SQLException;
    }
}

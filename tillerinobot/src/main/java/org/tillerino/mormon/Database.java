package org.tillerino.mormon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.tillerino.mormon.Mapping.FieldManager;
import org.tillerino.mormon.Persister.Action;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;

/**
 * Wrapper around a {@link Connection} which is used to create {@link Persister}
 * and {@link Loader} instances along with some other convenience methods.
 *
 * <p>
 * This class will close the underlying {@link Connection} when closed. It
 * implements {@link AutoCloseable}, so it is best used in a try-with block. It
 * is assumed that the connection comes from a pool and closing the connection
 * implementation will only return the actual connection back to the pool.
 */
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public record Database(Connection connection) implements AutoCloseable {

	public PreparedStatement prepare(String query) throws SQLException {
		return connection.prepareStatement(query);
	}

	/**
	 * @param query not null. query after table name. if you have conditions, start with "where".
	 */
	public <T> Loader<T> loader(Class<? extends T> cls, String query) throws SQLException {
		return Loader.createLoader(this, cls, query, false);
	}

	/**
	 * @param query not null. query after table name. if you have conditions, start with "where".
	 */
	public <T> Loader<T> streamingLoader(Class<? extends T> cls, String query) throws SQLException {
		return Loader.createLoader(this, cls, query, true);
	}

	/**
	 * A loader for the key columns.
	 */
	public <T> Loader<T> loader(Class<? extends T> cls) throws SQLException {
		return loader(cls, Loader.getWhereQueryForKeyColumns(cls));
	}

	public <T> Persister<T> persister(Class<T> cls, Action action) throws SQLException {
		return new Persister<T>(this, cls, action);
	}

	/**
	 * Convenience method to persist a single object.
	 */
	public <T> int persist(@Nonnull @NonNull T obj, Action a) throws SQLException {
		try (@SuppressWarnings("unchecked")
				Persister<T> persister = persister((Class<T>) obj.getClass(), a)) {
			return persister.persist(obj);
		}
	}

	/**
	 * Deletes one or multiple values from the database. See {@link KeyColumn}.
	 * @param multiple if true, the key values may be shorter than the array in the {@link KeyColumn} annotation.
	 */
	public <T> int delete(Class<T> cls, boolean multiple, Object... keyValues) throws SQLException {
		String query = Loader.getWhereQueryForKeyColumns(cls, multiple, keyValues);

		try(PreparedStatement s = prepare("delete from `" + Mapping.getOrCreateMapping(cls).table() + "` " + query)) {
			Loader.setParameters(s, keyValues);

			return s.executeUpdate();
		}
	}

	public <T> int delete(@NonNull T obj) throws SQLException {
		String query = Loader.getWhereQueryForKeyColumns(obj.getClass());
		Mapping<? extends Object> mapping = Mapping.getOrCreateMapping(obj.getClass());

		try(PreparedStatement s = prepare("delete from `" + mapping.table() + "` " + query)) {
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
		try(PreparedStatement s = prepare("select count(*) from `" + Mapping.getOrCreateMapping(cls).table() + "` " + query)) {
			Loader.setParameters(s, values);
			ResultSet set = s.executeQuery();
			set.next();
			return set.getLong(1);
		}
	}

	/**
	 * Loads a unique value from the database. See {@link KeyColumn}.
	 */
	@NonNull
	public <T> Optional<T> loadUnique(Class<T> cls, Object... keyValues) throws SQLException {
		String query = Loader.getWhereQueryForKeyColumns(cls, false, keyValues);

		try(Loader<T> loader = loader(cls, query)) {
			return loader.queryUnique((Object[]) keyValues);
		}
	}

	@Override
	public void close() throws SQLException {
		// We get a pooled connection, so this close just returns the connection to the pool.
		connection.close();
	}
}

package org.tillerino.mormon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.IterableUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wraps a {@link PreparedStatement} to load Java objects from the database.
 *
 * <p>
 * This class will close the underlying {@link PreparedStatement} when closed.
 * It implements {@link AutoCloseable}, so it is best used in a try-with block.
 */
@RequiredArgsConstructor
public class Loader<T> implements AutoCloseable {
	private final PreparedStatement statement;
	private final Mapping<? extends T> mapping;

	/**
	 * @param query not null. query after table name. if you have conditions, start with "where".
	 */
	static <T> Loader<T> createLoader(Database conn, Class<? extends T> cls, @NonNull String query, boolean stream) throws SQLException {
		Mapping<? extends T> mapping = Mapping.getOrCreateMapping(cls);
		String fullQuery = "select " + mapping.fields() + " from `" + mapping.table() + "` " + query;
		return getLoaderFullQuery(conn, cls, stream, mapping, fullQuery);
	}

	@SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE", justification = "We instead make sure that the statment is closed.")
	private static <T> Loader<T> getLoaderFullQuery(Database conn, Class<? extends T> cls, boolean stream,
			Mapping<? extends T> mapping, String fullQuery) throws SQLException {
		PreparedStatement statement;
		if(stream) {
			statement = conn.connection().prepareStatement(fullQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);
		} else {
			statement = conn.prepare(fullQuery);
		}
		return new Loader<>(statement, mapping);
	}

	/**
	 * @param parameters these must match the query which was used to create this loader.
	 *        They are set via {@link PreparedStatement#setObject(int, Object)} and must
	 *        only be of types that can handled natively by JDBC, e.g. boxed primitives and String.
	 * @return only valid as long as the Loader is not closed.
	 *         See {@link #queryList(Object...)} for a convenient alternative.
	 */
	@SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION", justification = "We instead make sure that the statment is closed.")
	public Iterable<T> query(Object ... parameters) throws SQLException {
		setParameters(statement, parameters);

		// execute right away, not in lambda for earlier error messages
		final ResultSet set = statement.executeQuery();

		return () -> new ResultSetIterator(set, mapping);
	}

	public List<T> queryList(Object... parameters) throws SQLException {
		return IterableUtils.toList(query(parameters));
	}
	
	/**
	 * returns one result from the query
	 * @param params
	 * @return null if there are no results
	 * @throws SQLException on general SQLException or if result was not unique
	 */
	@NonNull
	public Optional<T> queryUnique(Object ... params) throws SQLException {
		Iterator<T> iterator = query(params).iterator();
		
		if(!iterator.hasNext()) {
			return Optional.empty();
		}
		
		T ret = iterator.next();
		
		if(iterator.hasNext()) {
			throw new SQLException("Result not unique!");
		}
		
		return Optional.of(ret);
	}

	@Override
	public void close() throws SQLException {
		statement.close();
	}

	public static void setParameters(PreparedStatement preparedStatement, Object... parameters) throws SQLException {
		int expectedParameterCount = preparedStatement.getParameterMetaData().getParameterCount();
		if (expectedParameterCount != parameters.length) {
			throw new SQLException("Expected " + expectedParameterCount + " parameters but received " + parameters.length);
		}

		for (int i = 0; i < parameters.length; i++) {
			preparedStatement.setObject(i + 1, parameters[i]);
		}
	}

	static <T> String getWhereQueryForKeyColumns(Class<T> cls) {
		if (!cls.isAnnotationPresent(KeyColumn.class)) {
			throw new RuntimeException(cls.getName());
		}

		String[] keyColumns = cls.getAnnotation(KeyColumn.class).value();

		StringBuilder query = new StringBuilder();

		for (int i = 0; i < keyColumns.length; i++) {
			query.append((i > 0 ? " and " : "where ") + "`" + keyColumns[i] + "` = ?");
		}
		return query.toString();
	}

}

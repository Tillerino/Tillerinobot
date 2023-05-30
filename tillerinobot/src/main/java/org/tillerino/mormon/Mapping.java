package org.tillerino.mormon;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

/**
 * Utility class that handles mapping between fields and their values in a JDBC
 * {@link PreparedStatement} or {@link ResultSet}.
 *
 * <p>
 * Data can only be mapped for POJOs, i.e. objects with getters and setters as
 * created by e.g. Lombok's {@link Data} annotation.
 * There is currently no support for constructor-based setting of properties
 * or records.
 *
 * <p>
 * Since data is mapped from a {@link ResultSet}, it is possible to map from a relation
 * other than a database table, e.g. a view.
 * Note that the order of the fields of the class at runtime is used to determine the
 * index of the property in the result set / prepared statement.
 * To create suitable statements, use the {@link #fields()} string.
 */
@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public record Mapping<T>(String fields, String questionMarks, String table, List<FieldManager<?>> fieldManagers) {
	private static final Map<Class<?>, TypeHandler<?>> typeHandlers = Map.ofEntries(
		Map.entry(int.class, new DefaultTypeHandler(Types.INTEGER)),
		Map.entry(long.class, new DefaultTypeHandler(Types.BIGINT)),
		Map.entry(double.class, new DefaultTypeHandler(Types.DOUBLE)),
		Map.entry(boolean.class, new DefaultTypeHandler(Types.BOOLEAN)),
		Map.entry(Integer.class, new DefaultTypeHandler(Types.INTEGER)),
		Map.entry(Long.class, new DefaultTypeHandler(Types.BIGINT)),
		Map.entry(Double.class, new DefaultTypeHandler(Types.DOUBLE)),
		Map.entry(Boolean.class, new DefaultTypeHandler(Types.BOOLEAN)),
		Map.entry(String.class, new DefaultTypeHandler(Types.VARCHAR)),
		Map.entry(byte[].class, new TypeHandler<byte[]>() {
			@Override
			public void toStatement(byte[] object, PreparedStatement statement, int parameterIndex) throws SQLException {
				statement.setBytes(parameterIndex, object);
			}

			@Override
			public byte[] fromResultSet(ResultSet resultSet, int parameterIndex) throws SQLException {
				return resultSet.getBytes(parameterIndex);
			}
		}));

	private static final Map<Class<?>, Mapping<?>> mappings = new ConcurrentHashMap<>();

	void set(Object obj, PreparedStatement statement) throws SQLException {
		for (FieldManager<?> fieldManager : fieldManagers) {
			try {
				fieldManager.toStatement(obj, statement);
			} catch (Exception e) {
				throw new ContextedRuntimeException(e)
					.addContextValue("field", fieldManager.field);
			}
		}
	}

	void get(Object obj, ResultSet resultSet) throws SQLException {
		for (FieldManager<?> fieldManager : fieldManagers) {
			try {
				fieldManager.fromResultSet(obj, resultSet);
			} catch (Exception e) {
				throw new ContextedRuntimeException(e)
					.addContextValue("field", fieldManager.field);
			}
		}
	}

	static <T> Mapping<T> getOrCreateMapping(Class<T> cls) {
		@SuppressWarnings("unchecked")
		Mapping<T> mapping = (Mapping<T>) mappings.computeIfAbsent(cls, Mapping::createMapping);
		return mapping;
	}

	private static <T> Mapping<T> createMapping(Class<T> cls) {
		List<FieldManager<?>> fieldManagers = createFieldManagers(cls);

		String fieldList = fieldManagers.stream()
				.map(sg -> getColumnName(sg.field()))
				.map(col -> "`" + col + "`")
				.collect(joining(", "));
		String questionMarks = fieldManagers.stream()
				.map(sg -> "?")
				.collect(joining(", "));

		return new Mapping<>(fieldList, questionMarks, getTableName(cls), fieldManagers);
	}

	private static <T> List<FieldManager<?>> createFieldManagers(Class<T> cls) {
		List<Field> fields = collectRelevantFields(cls);

		sortKeyColumnsToFront(cls, fields);

		List<FieldManager<?>> fieldManagers = new ArrayList<>();
		for (Field f : fields) {
			// sql starts counting at 1
			fieldManagers.add(FieldManager.create(f, fieldManagers.size() + 1));
		}
		return fieldManagers;
	}

	private static <T> List<Field> collectRelevantFields(Class<T> cls) {
		List<Field> fields = new ArrayList<>();
		for(Class<?> curCls = cls; !curCls.equals(Object.class); curCls = curCls.getSuperclass()) {
			for (Field f : curCls.getDeclaredFields()) {
				if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
					continue;
				}
				fields.add(f);
			}
		}
		return fields;
	}

	private static <T> void sortKeyColumnsToFront(Class<T> cls, List<Field> fields) {
		List<String> keyColumns = Optional.ofNullable(cls.getAnnotation(KeyColumn.class))
				.map(a -> List.of(a.value()))
				.orElse(Collections.emptyList());
		fields.sort(Comparator.comparingInt(f -> {
			int index = keyColumns.indexOf(f.getName());
			return index < 0 ? Integer.MAX_VALUE : index;
		}));
	}

	private static String getTableName(Class<?> cls) {
		Table pc = cls.getAnnotation(Table.class);
		if (pc == null) {
			throw new RuntimeException("Table annotation not present on " + cls);
		}
		if (pc.value().length() == 0) {
			throw new RuntimeException("must provide table name in Table annotation");
		}
		return pc.value();
	}

	private static String getColumnName(Field f) {
		Column p = f.getAnnotation(Column.class);
		if (p != null && p.value().length() > 0) {
			return p.value();
		}
		return f.getName();
	}

	record FieldManager<T>(
			Field field,
			int index,
			TypeHandler<T> call,
			FailableFunction<Object, Object, ReflectiveOperationException> getter,
			FailableBiConsumer<Object, Object, ReflectiveOperationException> setter) {

		void fromResultSet(Object obj, ResultSet resultSet) throws SQLException, ReflectiveOperationException {
			setter.accept(obj, call.fromResultSet(resultSet, index));
		}

		void toStatement(Object obj, PreparedStatement statement) throws SQLException, ReflectiveOperationException {
			@SuppressWarnings("unchecked")
			T value = (T) getter.apply(obj);
			call.toStatement(value, statement, index);
		}

		static <T> FieldManager<T> create(Field f, int indexInStatement) {
			@SuppressWarnings("unchecked")
			TypeHandler<T> typeHandler = (TypeHandler<T>) typeHandlers.get(f.getType());
			if (typeHandler == null) {
				throw new RuntimeException(f.getName() + "" + f.getType() + "");
			}

			if (Modifier.isPublic(f.getModifiers())) {
				return new FieldManager<>(f, indexInStatement, typeHandler, f::get, f::set);
			} else {
				Method getter;
				try {
					String prefix = "get";
					if(f.getType().equals(boolean.class) || f.getType().equals(Boolean.class)) {
						prefix = "is";
					}
					getter = f.getDeclaringClass().getMethod(prefix + StringUtils.capitalize(f.getName()));
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("field " + f + " not public, but no getter!", e);
				}

				Method setter;
				try {
					setter = f.getDeclaringClass().getMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("field " + f + " not public, but no setter!", e);
				}

				return new FieldManager<>(f, indexInStatement, typeHandler, getter::invoke, setter::invoke);
			}
		}
	}

	interface TypeHandler<T> {
		void toStatement(T object, PreparedStatement statement, int parameterIndex) throws SQLException;

		T fromResultSet(ResultSet resultSet, int parameterIndex) throws SQLException;
	}

	private record DefaultTypeHandler(int sqlType) implements TypeHandler<Object> {
		@Override
		public void toStatement(Object object,
				PreparedStatement statement, int parameterIndex) throws SQLException {
			statement.setObject(parameterIndex, object, sqlType);
		}

		@Override
		public Object fromResultSet(ResultSet resultSet, int parameterIndex) throws SQLException {
			return unwrapBlob(resultSet.getObject(parameterIndex));
		}

		private static Object unwrapBlob(Object object) throws SQLException {
			return object instanceof Blob blob ? blob.getBytes(1, (int) blob.length()) : object;
		}
	}
}

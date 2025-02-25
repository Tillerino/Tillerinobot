package org.tillerino.mormon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.tillerino.mormon.Persister.Action;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.PhaseTimer;

@Slf4j
@Singleton
public class DatabaseManager implements AutoCloseable {
	private final Properties properties;

	private final GenericObjectPool<PoolableConnection> pool;

	@Inject
	@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
	public DatabaseManager(@Named("mysql") Properties properties) {
		this.properties = new Properties(properties);

		GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
		config.setTestOnBorrow(true);
		config.setMaxTotal(32);
		config.setMaxIdle(32);
		config.setLifo(true);
		config.setSoftMinEvictableIdleTime(Duration.ofMinutes(1));
		config.setJmxNameBase(getClass().getPackage().getName() + ":type=" + getClass().getSimpleName() + ",name=");

		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(this::createConnection, null);
		pool = new GenericObjectPool<>(poolableConnectionFactory, config);
		poolableConnectionFactory.setPool(pool);
	}

	private Connection createConnection() throws SQLException  {
		// This is a workaround for MySQL driver loading not working in tomcat
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		String host = properties.getProperty("host");
		String port = properties.getProperty("port");
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		String database = properties.getProperty("database", "osupp");

		return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
				+ "?user=" + user + "&password=" + password
				+ "&useUnicode=true&characterEncoding=utf-8&rewriteBatchedStatements=true&useSSL=false");
	}

	public static Properties loadMysqlPropertiesFromEnv() {
		Properties properties = new Properties();

		replaceWithEnv(properties, "MYSQL_HOST", "host");
		replaceWithEnv(properties, "MYSQL_PORT", "port");
		replaceWithEnv(properties, "MYSQL_USER", "user");
		replaceWithEnv(properties, "MYSQL_PASSWORD", "password");
		replaceWithEnv(properties, "MYSQL_DATABASE", "database");
		ensurePropertyPresent(properties, "host");
		ensurePropertyPresent(properties, "port");
		ensurePropertyPresent(properties, "user");
		ensurePropertyPresent(properties, "password");

		return properties;
	}

	private static void ensurePropertyPresent(Properties properties, String string) {
		if (!properties.containsKey(string)) {
			throw new RuntimeException("Mysql connection property missing: " + string);
		}
	}

	private static void replaceWithEnv(Properties properties, String env, String property) {
		String value = System.getenv(env);
		if (value != null) {
			properties.put(property, value);
		}
	}

	public Database getDatabase() {
		try(var _ = PhaseTimer.timeTask("borrow connection")) {
			return new Database(pool.borrowObject());
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (SQLRecoverableException e) {
			log.warn("Recoverable exception while borrowing connection. Assuming maintenance.", e);
			throw new MaintenanceException("Database maintenance");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		pool.close();
	}

	public <T> StringTemplate.Processor<List<T>, SQLException> selectList(Class<T> cls) {
		return st -> {
			try (Database db = getDatabase()) {
				return db.selectList(cls).process(st);
			}
		};
	}

	public <T> StringTemplate.Processor<Optional<T>, SQLException> selectUnique(Class<T> cls) {
		return st -> {
			try (Database db = getDatabase()) {
				return db.selectUnique(cls).process(st);
			}
		};
	}

	/**
	 * Borrows a connection and calls {@link Database#persist(Object, Action)}
	 */
	public <T> int persist(@Nonnull @NonNull T obj, Action a) throws SQLException {
		try (Database db = getDatabase()) {
			return db.persist(obj, a);
		}
	}

	/**
	 * Borrows a connection and calls {@link Database#delete(Object)}
	 */
	public <T> int delete(@NonNull T obj) throws SQLException {
		try (Database db = getDatabase()) {
			return db.delete(obj);
		}
	}
}

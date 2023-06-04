package tillerino.tillerinobot;

import static tillerino.tillerinobot.MysqlContainer.mysql;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;

/**
 * Creates a MySQL instance in running in Docker.
 */
@TestModule(AbstractDatabaseTest.DockeredMysqlModule.class)
@RunWith(InjectionRunner.class)
public abstract class AbstractDatabaseTest {
	public static class DockeredMysqlModule extends AbstractModule {
		@Provides
		@Named("mysql")
		Properties myqslProperties() {
			Properties props = new Properties();
			props.put("host", mysql().getHost());
			props.put("port", "" + mysql().getMappedPort(3306));
			props.put("user", mysql().getUsername());
			props.put("password", mysql().getPassword());
			props.put("database", mysql().getDatabaseName());
			return props;
		}
	}

	@Rule
	public TestRule resetMysql = new MysqlDatabaseLifecycle();

	@Inject
	protected DatabaseManager dbm;
	protected Database db;

	@Before
	public void createEntityManager() {
		db = dbm.getDatabase();
	}

	@After
	public void closeEntityManager() throws SQLException {
		db.close();
	}
}

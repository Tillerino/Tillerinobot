package tillerino.tillerinobot;

import static tillerino.tillerinobot.MysqlContainer.mysql;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import com.google.inject.Provides;
import com.mysql.cj.jdbc.MysqlDataSource;

import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.data.repos.ActualBeatmapRepository;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.util.MysqlModule;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;

/**
 * Creates a MySQL instance in running in Docker.
 */
@TestModule(AbstractDatabaseTest.DockeredMysqlModule.class)
@RunWith(InjectionRunner.class)
public abstract class AbstractDatabaseTest {
	public static class DockeredMysqlModule extends MysqlModule {
		protected DataSource dataSource() {
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setURL(mysql().getJdbcUrl());
			dataSource.setUser(mysql().getUsername());
			dataSource.setPassword(mysql().getPassword());
			return dataSource;
		}

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
	protected EntityManagerFactory emf;
	@Inject
	protected ThreadLocalAutoCommittingEntityManager em;
	@Inject
	protected GivenRecommendationRepository recommendationsRepo;
	@Inject
	protected ActualBeatmapRepository beatmapFilesRepo;
	@Inject
	protected GivenRecommendationRepository givenRecommendationRepository;
	private ResetEntityManagerCloseable reset;

	@Inject
	protected DatabaseManager dbm;
	protected Database db;

	@Before
	public void createEntityManager() {
		reset = em.withNewEntityManager();
		db = dbm.getDatabase();
	}

	@After
	public void closeEntityManager() throws SQLException {
		reset.close();
		db.close();
	}

	protected void reloadEntityManager() {
		reset.close();
		reset = em.withNewEntityManager();
	}
}

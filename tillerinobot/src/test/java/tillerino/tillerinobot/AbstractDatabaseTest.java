package tillerino.tillerinobot;

import static tillerino.tillerinobot.MysqlContainer.mysql;

import dagger.Component;
import java.sql.SQLException;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.data.*;

/** Creates a MySQL instance in running in Docker. */
public abstract class AbstractDatabaseTest {
    /** Use this if your child test requires no further injection except the database. */
    @Component(modules = DockeredMysqlModule.class)
    @Singleton
    public interface Injector {
        void inject(AbstractDatabaseTest t);
    }

    @dagger.Module(includes = RepoModule.class)
    public interface DockeredMysqlModule {
        @dagger.Provides
        @Named("mysql")
        static Properties myqslProperties() {
            Properties props = new Properties();
            props.put("host", mysql().getHost());
            props.put("port", "" + mysql().getMappedPort(3306));
            props.put("user", mysql().getUsername());
            props.put("password", mysql().getPassword());
            props.put("database", mysql().getDatabaseName());
            return props;
        }
    }

    @RegisterExtension
    public MysqlDatabaseLifecycle resetMysql = new MysqlDatabaseLifecycle();

    @Inject
    protected DatabaseManager dbm;

    @Inject
    protected BotUserData.Repo botUserDataRepo;

    @Inject
    protected BotUser.Repo botUserRepo;

    @Inject
    protected DiffEstimate.Repo diffEstimateRepo;

    @Inject
    protected Player.Repo playerRepo;

    @Inject
    protected ActualBeatmap.Repo actualBeatmapRepo;

    @Inject
    protected ApiScore.Repo apiScoreRepo;

    @Inject
    protected UserTop50Entry.Repo userTop50Repo;

    @Inject
    protected UserNameMapping.Repo userNameMappingRepo;

    @Inject
    protected GivenRecommendation.Repo givenRecommendationRepo;

    @Inject
    protected ApiBeatmap.Repo apiBeatmapRepo;

    @Inject
    protected ApiUser.Repo apiUserRepo;

    protected Database db;

    @BeforeEach
    public void createEntityManager() {
        db = dbm.getDatabase();
    }

    @AfterEach
    public void closeEntityManager() throws SQLException {
        if (db != null) {
            db.close();
        }
        dbm.close();
    }
}

package tillerino.tillerinobot;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;
import org.tillerino.ppaddict.web.data.repos.PpaddictLinkKeyRepository;
import org.tillerino.ppaddict.web.data.repos.PpaddictUserRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import tillerino.tillerinobot.data.repos.ActualBeatmapRepository;
import tillerino.tillerinobot.data.repos.BotConfigRepository;
import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.repos.UserNameMappingRepository;
import tillerino.tillerinobot.data.util.RepositoryModule;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;

/**
 * Creates an embedded HSQL database for tests.
 */
@TestModule(AbstractDatabaseTest.CreateInMemoryDatabaseModule.class)
@RunWith(InjectionRunner.class)
public abstract class AbstractDatabaseTest {
	public static class CreateInMemoryDatabaseModule extends AbstractModule {
		EntityManagerFactory emf;
		@Singleton
		@Provides
		public EntityManagerFactory newEntityManagerFactory() {
			if (emf != null) {
				return emf;
			}
			EclipseLinkJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
			vendorAdapter.setGenerateDdl(true);

			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setJpaVendorAdapter(vendorAdapter);
			Map<String, Object> jpaProperties = new LinkedHashMap<>();
			jpaProperties.put(PersistenceUnitProperties.WEAVING, "false");
			jpaProperties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
			factory.setJpaPropertyMap(jpaProperties);
			factory.setPackagesToScan("tillerino.tillerinobot.data", "org.tillerino.ppaddict.web.data");
			factory.setDataSource(dataSource());
			factory.afterPropertiesSet();

			return emf = factory.getObject();
		}

		protected DataSource dataSource() {
			return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build();
		}

		@Override
		protected void configure() {
			install(new RepositoryModule());
		}
	}
	@Inject
	protected EntityManagerFactory emf;
	@Inject
	protected ThreadLocalAutoCommittingEntityManager em;
	@Inject
	protected UserNameMappingRepository userNameMappingRepo;
	@Inject
	protected GivenRecommendationRepository recommendationsRepo;
	@Inject
	protected BotUserDataRepository userDataRepository;
	@Inject
	protected ActualBeatmapRepository beatmapFilesRepo;
	@Inject
	protected PpaddictUserRepository ppaddictUserRepository;
	@Inject
	protected PpaddictLinkKeyRepository ppaddictLinkKeyRepository;
	@Inject
	protected BotConfigRepository botConfigRepository;
	@Inject
	protected GivenRecommendationRepository givenRecommendationRepository;
	private ResetEntityManagerCloseable reset;

	@Before
	public void createEntityManager() {
		reset = em.withNewEntityManager();
	}

	@After
	public void closeEntityManager() {
		ppaddictUserRepository.deleteAll();
		ppaddictLinkKeyRepository.deleteAll();
		botConfigRepository.deleteAll();
		givenRecommendationRepository.deleteAll();
		reset.close();
	}

	protected void reloadEntityManager() {
		reset.close();
		reset = em.withNewEntityManager();
	}
}

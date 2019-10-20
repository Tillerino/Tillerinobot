package tillerino.tillerinobot;

import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import tillerino.tillerinobot.data.repos.ActualBeatmapRepository;
import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.repos.UserNameMappingRepository;
import tillerino.tillerinobot.data.util.RepositoryModule;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

public abstract class AbstractDatabaseTest {
	public static class CreateInMemoryDatabaseModule extends AbstractModule {
		@Provides
		public EntityManagerFactory newEntityManagerFactory() {
			HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
			vendorAdapter.setGenerateDdl(true);

			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setJpaVendorAdapter(vendorAdapter);
			factory.setPackagesToScan("tillerino.tillerinobot.data");
			factory.setDataSource(new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build());
			factory.afterPropertiesSet();

			return factory.getObject();
		}

		@Override
		protected void configure() {
			install(new RepositoryModule());
		}
	}
	protected static Injector injector;
	                 
	protected static EntityManagerFactory emf;
	                 
	protected static ThreadLocalAutoCommittingEntityManager em;
	                 
	protected static UserNameMappingRepository userNameMappingRepo;
	protected static GivenRecommendationRepository recommendationsRepo;
	protected static BotUserDataRepository userDataRepository;
	protected static ActualBeatmapRepository beatmapFilesRepo;
	
	@BeforeClass
	public static void injectAll() {
		injector = Guice.createInjector(new CreateInMemoryDatabaseModule());

		emf = injector.getInstance(EntityManagerFactory.class);
		
		em = injector.getInstance(ThreadLocalAutoCommittingEntityManager.class);
		
		userNameMappingRepo = injector.getInstance(UserNameMappingRepository.class);
		recommendationsRepo = injector.getInstance(GivenRecommendationRepository.class);
		userDataRepository = injector.getInstance(BotUserDataRepository.class);
		beatmapFilesRepo = injector.getInstance(ActualBeatmapRepository.class);
	}
	
	@AfterClass
	public static void closeEmf() {
		emf.close();
	}
	
	@Before
	public void createEntityManager() {
		em.setThreadLocalEntityManager(emf.createEntityManager());
	}
	
	@After
	public void closeEntityManager() {
		em.close();
	}
}

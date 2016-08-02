package tillerino.tillerinobot;

import javax.persistence.EntityManagerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import tillerino.tillerinobot.data.repos.UserNameMappingRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;

public class AbstractDatabaseTest {
	protected static EntityManagerFactory emf;
	
	protected static final ThreadLocalAutoCommittingEntityManager em = new ThreadLocalAutoCommittingEntityManager();
	
	private static JpaRepositoryFactory jpaRepositoryFactory;
	protected static UserNameMappingRepository userNameMappingRepo;
	
	@BeforeClass
	public static void prepareEntityManagerFactory() {
		if (emf != null) {
			return;
		}
		
		emf = newEntityManagerFactory();
		em.setThreadLocalEntityManager(emf.createEntityManager());
		jpaRepositoryFactory = new JpaRepositoryFactory(em);
		userNameMappingRepo = jpaRepositoryFactory.getRepository(UserNameMappingRepository.class);
		em.close();
	}
	
	@Before
	public void createEntityManager() {
		em.setThreadLocalEntityManager(emf.createEntityManager());
	}
	
	@After
	public void closeEntityManager() {
		em.close();
	}
	
	public static EntityManagerFactory newEntityManagerFactory() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("tillerino.tillerinobot.data");
		factory.setDataSource(new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build());
		factory.afterPropertiesSet();

		return factory.getObject();
	}
}

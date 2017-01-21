package tillerino.tillerinobot.data.util;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.repos.UserNameMappingRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class RepositoryModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(EntityManager.class).to(
				ThreadLocalAutoCommittingEntityManager.class);
		bind(ThreadLocalAutoCommittingEntityManager.class).in(Singleton.class);
	}

	private UserNameMappingRepository userNameRepo;
	private GivenRecommendationRepository recRepo;
	private BotUserDataRepository userDataRepo;

	@Provides
	@Singleton
	public JpaRepositoryFactory getRepoFactory(EntityManagerFactory emf,
			ThreadLocalAutoCommittingEntityManager em) {
		try {
			em.setThreadLocalEntityManager(emf.createEntityManager());
			try {
				JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
				createRepositories(factory);
				return factory;
			} finally {
				em.close();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	protected void createRepositories(JpaRepositoryFactory factory) {
		userNameRepo = factory.getRepository(UserNameMappingRepository.class);
		recRepo = factory.getRepository(GivenRecommendationRepository.class);
		userDataRepo = factory.getRepository(BotUserDataRepository.class);
	}

	@Provides
	@Singleton
	public UserNameMappingRepository userNameRepo(JpaRepositoryFactory factory) {
		return userNameRepo;
	}

	@Provides
	@Singleton
	public GivenRecommendationRepository recRepo(JpaRepositoryFactory factory) {
		return recRepo;
	}

	@Provides
	@Singleton
	public BotUserDataRepository userDataRepo(JpaRepositoryFactory factory) {
		return userDataRepo;
	}
}

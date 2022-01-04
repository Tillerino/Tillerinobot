package tillerino.tillerinobot.data.util;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.tillerino.ppaddict.web.data.repos.PpaddictLinkKeyRepository;
import org.tillerino.ppaddict.web.data.repos.PpaddictUserRepository;

import tillerino.tillerinobot.data.repos.ActualBeatmapRepository;
import tillerino.tillerinobot.data.repos.BotConfigRepository;
import tillerino.tillerinobot.data.repos.BotUserDataRepository;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.repos.UserNameMappingRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;

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
	private ActualBeatmapRepository beatmapFilesRepo;
	private PpaddictUserRepository ppaddictUserRepository;
	private PpaddictLinkKeyRepository ppaddictLinkKeyRepository;
	private BotConfigRepository botConfigRepository;

	@Provides
	@Singleton
	public JpaRepositoryFactory getRepoFactory(EntityManagerFactory emf,
			ThreadLocalAutoCommittingEntityManager em) {
		try(ResetEntityManagerCloseable cl = em.withNewEntityManager()) {
			JpaRepositoryFactory factory = new JpaRepositoryFactory(em);
			createRepositories(factory);
			return factory;
		}
	}

	protected void createRepositories(JpaRepositoryFactory factory) {
		userNameRepo = factory.getRepository(UserNameMappingRepository.class);
		recRepo = factory.getRepository(GivenRecommendationRepository.class);
		userDataRepo = factory.getRepository(BotUserDataRepository.class);
		beatmapFilesRepo = factory.getRepository(ActualBeatmapRepository.class);
		ppaddictUserRepository = factory.getRepository(PpaddictUserRepository.class);
		ppaddictLinkKeyRepository = factory.getRepository(PpaddictLinkKeyRepository.class);
		botConfigRepository = factory.getRepository(BotConfigRepository.class);
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

	@Provides
	@Singleton
	public ActualBeatmapRepository beatmapsRepository(JpaRepositoryFactory factory) {
		return beatmapFilesRepo;
	}

	@Provides
	@Singleton
	public PpaddictUserRepository ppaddictUserRepository(JpaRepositoryFactory factory) {
		return ppaddictUserRepository;
	}

	@Provides
	@Singleton
	public PpaddictLinkKeyRepository ppaddictLinkKeyRepository(JpaRepositoryFactory factory) {
		return ppaddictLinkKeyRepository;
	}

	@Provides
	@Singleton
	public BotConfigRepository botConfigRepository(JpaRepositoryFactory factory) {
		return botConfigRepository;
	}
}

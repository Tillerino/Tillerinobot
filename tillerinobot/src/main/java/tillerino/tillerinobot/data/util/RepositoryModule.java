package tillerino.tillerinobot.data.util;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

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
	}
}

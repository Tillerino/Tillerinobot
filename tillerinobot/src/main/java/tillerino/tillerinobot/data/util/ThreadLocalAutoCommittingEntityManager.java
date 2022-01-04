package tillerino.tillerinobot.data.util;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.EntityManagerProxy;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import tillerino.tillerinobot.IRCBot;

/**
 * <p>
 * A proxy class for {@link EntityManager}. It serves two purposes: Thread-local
 * proxy and auto-commit for save, delete and merge. Basically it's a horrible
 * hack because everything is a mess and we need direct control over a lot of
 * things because we're mixing too many frameworks (IRC, GWT, and JAX-RS) and
 * want to remain transactionless.
 * </p>
 * 
 * <p>
 * We use a thread-local proxy so we can inject one singleton of each
 * {@link JpaRepository} based on this EntityManager. This is easier than making
 * a thread-local version of the repository classes, since those are only
 * created at run-time. This means that the thread-local EntityManager instance
 * needs to be set at some point. It is set in
 * {@link IRCBot#onEvent(org.tillerino.ppaddict.chat.GameChatEvent)}, which covers the entire IRC
 * frontend. For the REST API, the EntityManager is set in the
 * {@link EntityManagerProxyFeature}.
 * </p>
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ThreadLocalAutoCommittingEntityManager implements
		EntityManagerProxy {
	private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();
	private final EntityManagerFactory emf;

	@Delegate(types = EntityManager.class)
	@Override
	public EntityManager getTargetEntityManager() throws IllegalStateException {
		EntityManager em = entityManager.get();
		if (em == null) {
			throw new IllegalStateException(
					"No EntityManager present for this Thread.");
		}
		return em;
	}

	@Override
	public <T> T merge(T entity) {
		return ensureTransaction(() -> getTargetEntityManager().merge(entity));
	}

	@Override
	public void persist(Object entity) {
		ensureTransaction(() -> getTargetEntityManager().persist(entity));
	}

	@Override
	public void remove(Object entity) {
		ensureTransaction(() -> getTargetEntityManager().remove(entity));
	}

	public EntityManager setThreadLocalEntityManager(EntityManager em) {
		EntityManager old = entityManager.get();
		entityManager.set(em);
		return old;
	}

	public boolean isThreadLocalEntityManagerPresent() {
		return entityManager.get() != null;
	}

	public <T> T ensureTransaction(Supplier<T> r) {
		EntityTransaction transaction = getTargetEntityManager()
				.getTransaction();
		boolean createTransaction = !transaction.isActive();
		if (createTransaction) {
			transaction.begin();
		}
		boolean rolledBack = false;
		try {
			return r.get();
		} catch (Exception e) {
			if (createTransaction) {
				rolledBack = true;
				transaction.rollback();
			}
			throw e;
		} finally {
			if (createTransaction && !rolledBack) {
				transaction.commit();
			}
		}
	}

	public void ensureTransaction(Runnable r) {
		ensureTransaction(() -> {
			r.run();
			return null;
		});
	}

	public ResetEntityManagerCloseable withNewEntityManager() {
		return new ResetEntityManagerCloseable(this, setThreadLocalEntityManager(emf.createEntityManager()));
	}

	@RequiredArgsConstructor
	public class ResetEntityManagerCloseable implements AutoCloseable {
		private final ThreadLocalAutoCommittingEntityManager tlem;
		private final EntityManager old;

		@Override
		public void close() {
			tlem.close();
			tlem.setThreadLocalEntityManager(old);
		}
	}
}
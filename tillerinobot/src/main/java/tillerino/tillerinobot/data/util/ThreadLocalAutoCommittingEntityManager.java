package tillerino.tillerinobot.data.util;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.EntityManagerProxy;

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
 * {@link IRCBot#onEvent(org.pircbotx.hooks.Event)}, which covers the entire IRC
 * frontend. For the REST API, the EntityManager is set in the
 * {@link EntityManagerProxyFeature}.
 * </p>
 */
public class ThreadLocalAutoCommittingEntityManager implements
		EntityManagerProxy {
	private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

	@Override
	public void clear() {
		getTargetEntityManager().clear();
	}

	@Override
	public void close() {
		getTargetEntityManager().close();
	}

	@Override
	public boolean contains(Object entity) {
		return getTargetEntityManager().contains(entity);
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		return getTargetEntityManager().createEntityGraph(rootType);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String graphName) {
		return getTargetEntityManager().createEntityGraph(graphName);
	}

	@Override
	public Query createNamedQuery(String name) {
		return getTargetEntityManager().createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return getTargetEntityManager().createNamedQuery(name, resultClass);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		return getTargetEntityManager().createNamedStoredProcedureQuery(name);
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return getTargetEntityManager().createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString,
			@SuppressWarnings("rawtypes") Class resultClass) {
		return getTargetEntityManager().createNativeQuery(sqlString,
				resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return getTargetEntityManager().createNativeQuery(sqlString,
				resultSetMapping);
	}

	@Override
	public Query createQuery(
			@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery) {
		return getTargetEntityManager().createQuery(deleteQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return getTargetEntityManager().createQuery(criteriaQuery);
	}

	@Override
	public Query createQuery(
			@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery) {
		return getTargetEntityManager().createQuery(updateQuery);
	}

	@Override
	public Query createQuery(String qlString) {
		return getTargetEntityManager().createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return getTargetEntityManager().createQuery(qlString, resultClass);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		return getTargetEntityManager().createStoredProcedureQuery(
				procedureName);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(
			String procedureName,
			@SuppressWarnings("rawtypes") Class... resultClasses) {
		return getTargetEntityManager().createStoredProcedureQuery(
				procedureName, resultClasses);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(
			String procedureName, String... resultSetMappings) {
		return getTargetEntityManager().createStoredProcedureQuery(
				procedureName, resultSetMappings);
	}

	@Override
	public void detach(Object entity) {
		getTargetEntityManager().detach(entity);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return getTargetEntityManager().find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey,
			LockModeType lockMode) {
		return getTargetEntityManager().find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey,
			LockModeType lockMode, Map<String, Object> properties) {
		return getTargetEntityManager().find(entityClass, primaryKey, lockMode,
				properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey,
			Map<String, Object> properties) {
		return getTargetEntityManager().find(entityClass, primaryKey,
				properties);
	}

	@Override
	public void flush() {
		getTargetEntityManager().flush();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return getTargetEntityManager().getCriteriaBuilder();
	}

	@Override
	public Object getDelegate() {
		return getTargetEntityManager();
	}

	@Override
	public EntityGraph<?> getEntityGraph(String graphName) {
		return getTargetEntityManager().getEntityGraph(graphName);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		return getTargetEntityManager().getEntityGraphs(entityClass);
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return getTargetEntityManager().getEntityManagerFactory();
	}

	@Override
	public FlushModeType getFlushMode() {
		return getTargetEntityManager().getFlushMode();
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		return getTargetEntityManager().getLockMode(entity);
	}

	@Override
	public Metamodel getMetamodel() {
		return getTargetEntityManager().getMetamodel();
	}

	@Override
	public Map<String, Object> getProperties() {
		return getTargetEntityManager().getProperties();
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return getTargetEntityManager().getReference(entityClass, primaryKey);
	}

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
	public EntityTransaction getTransaction() {
		return getTargetEntityManager().getTransaction();
	}

	@Override
	public boolean isJoinedToTransaction() {
		return getTargetEntityManager().isJoinedToTransaction();
	}

	@Override
	public boolean isOpen() {
		return getTargetEntityManager().isOpen();
	}

	@Override
	public void joinTransaction() {
		getTargetEntityManager().joinTransaction();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		getTargetEntityManager().lock(entity, lockMode);
	}

	@Override
	public void lock(Object entity, LockModeType lockMode,
			Map<String, Object> properties) {
		getTargetEntityManager().lock(entity, lockMode, properties);
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
	public void refresh(Object entity) {
		getTargetEntityManager().refresh(entity);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		getTargetEntityManager().refresh(entity, lockMode);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode,
			Map<String, Object> properties) {
		getTargetEntityManager().refresh(entity, lockMode, properties);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		getTargetEntityManager().refresh(entity, properties);
	}

	@Override
	public void remove(Object entity) {
		ensureTransaction(() -> {
			getTargetEntityManager().remove(entity);
		});
	}

	public void setThreadLocalEntityManager(EntityManager em) {
		entityManager.set(em);
	}
	
	public boolean isThreadLocalEntityManagerPresent() {
		return entityManager.get() != null;
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		getTargetEntityManager().setFlushMode(flushMode);
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		getTargetEntityManager().setProperty(propertyName, value);
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return getTargetEntityManager().unwrap(cls);
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
}
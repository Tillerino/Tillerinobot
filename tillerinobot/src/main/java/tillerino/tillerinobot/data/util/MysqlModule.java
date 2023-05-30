package tillerino.tillerinobot.data.util;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.tillerino.mormon.DatabaseManager;

import com.google.inject.Provides;

public class MysqlModule extends RepositoryModule {

	@Provides
	@Singleton
	public EntityManagerFactory emf(DataSource dataSource) {
		EclipseLinkJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(false);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		Map<String, Object> jpaProperties = new LinkedHashMap<>();
		jpaProperties.put(PersistenceUnitProperties.WEAVING, "false");
		jpaProperties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
		factory.setJpaPropertyMap(jpaProperties);
		factory.setPackagesToScan("tillerino.tillerinobot.data", "org.tillerino.ppaddict.web.data");
		factory.setDataSource(dataSource);
		factory.afterPropertiesSet();

		return factory.getObject();
	}

	@Provides
	@Singleton
	public DataSource dataSource(DatabaseManager db) {
		return db.getPoolingDatasource();
	}
}

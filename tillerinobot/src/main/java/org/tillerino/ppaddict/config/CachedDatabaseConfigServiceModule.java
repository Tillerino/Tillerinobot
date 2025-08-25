package org.tillerino.ppaddict.config;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;

@Module
public interface CachedDatabaseConfigServiceModule {
	@Binds ConfigService cached(CachingConfigService c);

	@Binds @Named("uncached") ConfigService uncached(DatabaseConfigService c);
}

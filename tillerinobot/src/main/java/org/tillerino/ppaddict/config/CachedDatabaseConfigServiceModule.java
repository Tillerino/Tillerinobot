package org.tillerino.ppaddict.config;

import dagger.Binds;
import dagger.Module;
import javax.inject.Named;

@Module
public interface CachedDatabaseConfigServiceModule {
    @Binds
    ConfigService cached(CachingConfigService c);

    @Binds
    @Named("uncached")
    ConfigService uncached(DatabaseConfigService c);
}

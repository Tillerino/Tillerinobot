package org.tillerino.ppaddict.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class CachedDatabaseConfigServiceModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(ConfigService.class).to(CachingConfigService.class);
		bind(ConfigService.class).annotatedWith(Names.named("uncached")).to(DatabaseConfigService.class);
	}
}

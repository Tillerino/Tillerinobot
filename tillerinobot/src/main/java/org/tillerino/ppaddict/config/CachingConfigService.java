package org.tillerino.ppaddict.config;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Singleton
public class CachingConfigService implements ConfigService {
	private final LoadingCache<String, Optional<String>> cache;

	@Inject
	public CachingConfigService(@Named("uncached") ConfigService delegate) {
		this.cache = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.SECONDS)
			.build(CacheLoader.from(delegate::config));
	}

	@Override
	public Optional<String> config(String key) {
		return cache.getUnchecked(key);
	}
}

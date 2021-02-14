package org.tillerino.ppaddict.config;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.data.repos.BotConfigRepository;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DatabaseConfigService implements ConfigService {
	private final BotConfigRepository repo;

	@Override
	public Optional<String> config(String key) {
		return repo.findById(key).map(item -> item.getValue());
	}
}

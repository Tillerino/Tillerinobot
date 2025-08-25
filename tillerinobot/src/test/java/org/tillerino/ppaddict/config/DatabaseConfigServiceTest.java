package org.tillerino.ppaddict.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Persister.Action;

import dagger.Component;
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.data.BotConfig;

public class DatabaseConfigServiceTest extends AbstractDatabaseTest {
	@Component(modules = {DockeredMysqlModule.class, CachedDatabaseConfigServiceModule.class})
	@Singleton
	interface Injector {
		void inject(DatabaseConfigServiceTest test);
	}

	{
		DaggerDatabaseConfigServiceTest_Injector.create().inject(this);
	}

	@Inject
	ConfigService config;

	@Test
	public void noConfigIsDefault() throws Exception {
		assertThat(config.scoresMaintenance()).isFalse();
	}

	@Test
	public void falsee() throws Exception {
		BotConfig botConfig = new BotConfig();
		botConfig.setPath("api-scores-maintenance");
		botConfig.setValue("false");
		db.persist(botConfig, Action.INSERT);
		assertThat(config.scoresMaintenance()).isFalse();
	}

	@Test
	public void truee() throws Exception {
		BotConfig botConfig = new BotConfig();
		botConfig.setPath("api-scores-maintenance");
		botConfig.setValue("true");
		db.persist(botConfig, Action.INSERT);
		assertThat(config.scoresMaintenance()).isTrue();
	}
}

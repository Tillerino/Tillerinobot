package org.tillerino.ppaddict.config;

import static org.assertj.core.api.Assertions.assertThat;

import dagger.Component;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
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

    @Inject
    BotConfig.Repo repo;

    @Test
    public void noConfigIsDefault() {
        assertThat(config.scoresMaintenance()).isFalse();
    }

    @Test
    public void falsee() throws SQLException {
        BotConfig botConfig = new BotConfig();
        botConfig.setPath("api-scores-maintenance");
        botConfig.setValue("false");
        repo.set(db, botConfig);
        assertThat(config.scoresMaintenance()).isFalse();
    }

    @Test
    public void truee() throws SQLException {
        BotConfig botConfig = new BotConfig();
        botConfig.setPath("api-scores-maintenance");
        botConfig.setValue("true");
        repo.set(db, botConfig);
        assertThat(config.scoresMaintenance()).isTrue();
    }
}

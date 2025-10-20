package org.tillerino.ppaddict.config;

import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import tillerino.tillerinobot.data.BotConfig;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DatabaseConfigService implements ConfigService {
    private final DatabaseManager dbm;

    @Override
    public Optional<String> config(String key) {
        try (Database db = dbm.getDatabase()) {
            return db.selectUnique(BotConfig.class)
                    .execute("where path = ", key)
                    .map(BotConfig::getValue);
        } catch (SQLException e) {
            throw new ContextedRuntimeException("Unable to load config", e).addContextValue("key", key);
        }
    }
}

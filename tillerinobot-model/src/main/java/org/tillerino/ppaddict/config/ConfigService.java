package org.tillerino.ppaddict.config;

import java.util.Optional;

public interface ConfigService {
    Optional<String> config(String key);

    default boolean scoresMaintenance() {
        return config("api-scores-maintenance").orElse("false").equals("true");
    }
}

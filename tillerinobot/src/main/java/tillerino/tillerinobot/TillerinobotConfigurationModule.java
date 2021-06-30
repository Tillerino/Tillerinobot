package tillerino.tillerinobot;

import java.util.function.Function;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TillerinobotConfigurationModule extends AbstractModule {
	@Override
	protected void configure() {
		Names.bindProperties(binder(), System.getProperties());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_SERVER", "tillerinobot.irc.server", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_PORT", "tillerinobot.irc.port", binder(), Integer::valueOf);
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_NICKNAME", "tillerinobot.irc.nickname", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_PASSWORD", "tillerinobot.irc.password", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_AUTOJOIN", "tillerinobot.irc.autojoin", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IGNORE", "tillerinobot.ignore", binder(), Boolean::valueOf);
	}

	public static void loadFromEnvironmentVariable(String env, String propertyName, Binder binder) {
		loadFromEnvironmentVariable(env, propertyName, binder, Function.identity());
	}

	public static <T> void loadFromEnvironmentVariable(String env, String propertyName, Binder binder,
			Function<String, T> parser) {
		String value = System.getenv(env);
		if (value != null) {
			log.debug("Loaded {} from environment variable {}", propertyName, env);
			T parsed = parser.apply(value);
			Class<T> cls = (Class<T>) parsed.getClass();
			binder.bind(Key.get(cls, Names.named(propertyName))).toInstance(parsed);
		}
	}
}

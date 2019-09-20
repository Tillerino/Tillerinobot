package tillerino.tillerinobot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TillerinobotConfigurationModule extends AbstractModule {
	@Override
	protected void configure() {
		loadProperties("/tillerinobot.git.properties", binder(), false);
		loadProperties("/tillerinobot.properties", binder(), true);
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_SERVER", "tillerinobot.irc.server", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_PORT", "tillerinobot.irc.port", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_NICKNAME", "tillerinobot.irc.nickname", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_PASSWORD", "tillerinobot.irc.password", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IRC_AUTOJOIN", "tillerinobot.irc.autojoin", binder());
		loadFromEnvironmentVariable("TILLERINOBOT_IGNORE", "tillerinobot.ignore", binder());
	}

	public static void loadProperties(String path, Binder binder) {
		loadProperties(path, binder, false);
	}

	public static void loadProperties(String path, Binder binder, boolean optional) {
		try (InputStream is = TillerinobotConfigurationModule.class.getResourceAsStream(path)) {
			if (is != null) {
				Properties properties = new Properties();
				properties.load(is);
				Names.bindProperties(binder, properties);
			} else {
				if (!optional) {
					throw new RuntimeException("Resource not found: " + path);
				}
				log.warn("Resource not found: {}", path);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void loadFromEnvironmentVariable(String env, String propertyName, Binder binder) {
		String value = System.getenv(env);
		if (value != null) {
			log.debug("Loaded {} from environment variable", propertyName);
			Properties properties = new Properties();
			properties.put(propertyName, value);
			Names.bindProperties(binder, properties);
		}
	}
}

package tillerino.tillerinobot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class TillerinobotConfigurationModule extends AbstractModule {
	@Override
	protected void configure() {
		loadProperties("/tillerinobot.git.properties", binder());
		loadProperties("/tillerinobot.properties", binder());
		Names.bindProperties(binder(), System.getProperties());
	}

	public static void loadProperties(String path, Binder binder) {
		try (InputStream is = TillerinobotConfigurationModule.class.getResourceAsStream(path)) {
			if (is == null) {
				throw new RuntimeException("Resource not found: " + path);
			}
			Properties properties = new Properties();
			properties.load(is);
			Names.bindProperties(binder, properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

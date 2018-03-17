package tillerino.tillerinobot.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Application;

import tillerino.tillerinobot.data.util.EntityManagerProxyFeature;

/**
 * @author Tillerino
 */
public class BotApiDefinition extends Application {
	Set<Object> resourceInstances = new HashSet<>();

	@Inject
	public BotApiDefinition(BotInfoService botInfo, BeatmapInfoService beatmapInfo,
			UserByIdService userById, EntityManagerProxyFeature proxyFeature, BeatmapsService beatmaps,
			AuthenticationFilter authentication, ApiLoggingFeature logging) {
		super();

		resourceInstances.add(botInfo);
		resourceInstances.add(beatmapInfo);
		resourceInstances.add(userById);
		resourceInstances.add(proxyFeature);
		resourceInstances.add(new DelegatingBeatmapsService(beatmaps));
		resourceInstances.add(authentication);
		resourceInstances.add(logging);
	}

	@Override
	public Set<Object> getSingletons() {
		return resourceInstances;
	}

	@Override
	public Set<Class<?>> getClasses() {
		return Collections.singleton(PrintMessageExceptionMapper.class);
	}
}

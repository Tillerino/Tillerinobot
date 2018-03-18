package tillerino.tillerinobot.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerResponseFilter;
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
		resourceInstances.add((ContainerResponseFilter) (requestContext, responseContext) ->  {
			// allow requests from github page, e.g. swagger UI.
			List<String> origin = requestContext.getHeaders().getOrDefault("Origin", Collections.emptyList());
			if (origin.stream().anyMatch(
					x -> x.startsWith("http://tillerino.github.io") || x.startsWith("https://tillerino.github.io"))) {
				origin.forEach(o -> responseContext.getHeaders().addAll("Access-Control-Allow-Origin", o));
			}
		});
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

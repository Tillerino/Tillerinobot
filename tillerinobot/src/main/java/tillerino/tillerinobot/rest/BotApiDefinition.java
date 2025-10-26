package tillerino.tillerinobot.rest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

/** @author Tillerino */
public class BotApiDefinition extends Application {
    final Set<Object> resourceInstances = new HashSet<>();

    @Inject
    public BotApiDefinition(
            BotInfoService botInfo,
            BeatmapInfoService beatmapInfo,
            UserByIdService userById,
            BeatmapsService beatmaps,
            AuthenticationFilter authentication,
            ApiLoggingFeature logging) {
        super();

        resourceInstances.add(botInfo);
        resourceInstances.add(beatmapInfo);
        resourceInstances.add(userById);
        resourceInstances.add(new DelegatingBeatmapsService(beatmaps));
        resourceInstances.add(authentication);
        resourceInstances.add(logging);
        resourceInstances.add((ContainerResponseFilter) (requestContext, responseContext) -> {
            // allow requests from github page, e.g. swagger UI.
            List<String> origin = requestContext.getHeaders().getOrDefault("Origin", Collections.emptyList());
            if (origin.stream().allMatch(x -> x.startsWith("https://tillerino.github.io"))) {
                origin.forEach(o -> responseContext.getHeaders().add("Access-Control-Allow-Origin", o));
                responseContext.getHeaders().add("Access-Control-Allow-Headers", "api-key");
            }
        });
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "We intentionally modify this externally. Sorry :D")
    @Override
    public Set<Object> getSingletons() {
        return resourceInstances;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(PrintMessageExceptionMapper.class);
    }
}

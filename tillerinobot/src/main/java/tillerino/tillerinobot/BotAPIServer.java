package tillerino.tillerinobot;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import tillerino.tillerinobot.data.util.EntityManagerProxyFeature;
import tillerino.tillerinobot.rest.AuthenticationFilter;
import tillerino.tillerinobot.rest.BeatmapInfoService;
import tillerino.tillerinobot.rest.BotInfoService;
import tillerino.tillerinobot.rest.PrintMessageExceptionMapper;
import tillerino.tillerinobot.rest.UserByIdService;

/**
 * @author Tillerino
 */
public class BotAPIServer extends Application {
	Set<Object> resourceInstances = new HashSet<>();

	@Inject
	public BotAPIServer(BotInfoService botInfo, BeatmapInfoService beatmapInfo,
			UserByIdService userById, EntityManagerProxyFeature proxyFeature,
			AuthenticationFilter authentication) {
		super();

		resourceInstances.add(botInfo);
		resourceInstances.add(beatmapInfo);
		resourceInstances.add(userById);
		resourceInstances.add(proxyFeature);
		resourceInstances.add(authentication);
	}

	@Override
	public Set<Object> getSingletons() {
		return resourceInstances;
	}

	@Override
	public Set<Class<?>> getClasses() {
		return Collections.singleton(PrintMessageExceptionMapper.class);
	}

	public static WebApplicationException getBadGateway(IOException e) {
		return exceptionFor(Status.fromStatusCode(502), e != null ? e.getMessage() : "Communication with the osu API server failed.");
	}
	
	public static WebApplicationException getNotFound(String message) {
		return exceptionFor(Status.NOT_FOUND, message);
	}

	public static WebApplicationException getUserMessage(UserException exception) {
		return getNotFound(exception.getMessage());
	}

	public static WebApplicationException exceptionFor(Status status, String message) {
		return new WebApplicationException(Response.status(status).entity(message).build());
	}

	public static WebApplicationException getInterrupted() {
		return exceptionFor(Status.SERVICE_UNAVAILABLE, "The server is being shutdown for maintenance");
	}
	
	public static Throwable refreshWebApplicationException(Throwable t) {
		if (t instanceof WebApplicationException) {
			return new WebApplicationException(t.getCause(), Response.fromResponse(((WebApplicationException) t).getResponse()).build());
		}
		return t;
	}
}

package tillerino.tillerinobot.rest;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response.Status;

import org.slf4j.MDC;
import org.tillerino.ppaddict.rest.AuthenticationService;

import lombok.RequiredArgsConstructor;

@KeyRequired
@Priority(Priorities.AUTHENTICATION)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AuthenticationFilter implements ContainerRequestFilter {
	private final AuthenticationService authentication;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		List<String> keys = requestContext.getUriInfo().getQueryParameters().get("k");
		if (keys == null || keys.isEmpty()) {
			throw new WebApplicationException("Please provide an API key", Status.UNAUTHORIZED);
		}
		try {
			authentication.findKey(keys.get(0));
		} catch (NotFoundException e) {
			throw new WebApplicationException("Unknown API key", Status.UNAUTHORIZED);
		}
		MDC.put("apiKey", keys.get(0));
	}
}

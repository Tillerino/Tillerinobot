package tillerino.tillerinobot.rest;

import java.io.IOException;
import java.util.Optional;

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
		String apiKey = Optional.ofNullable(requestContext.getUriInfo().getQueryParameters().get("k"))
				.flatMap(l -> l.stream().findFirst()).orElse(requestContext.getHeaderString("api-key"));

		try {
			if (apiKey == null) {
				throw new NotFoundException();
			}
			authentication.findKey(apiKey);
		} catch (NotFoundException e) {
			throw new WebApplicationException("Unknown API key", Status.UNAUTHORIZED);
		}
		MDC.put("apiKey", apiKey);
	}
}

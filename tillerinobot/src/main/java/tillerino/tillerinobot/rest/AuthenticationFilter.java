package tillerino.tillerinobot.rest;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.MdcUtils;

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
		// abbreviate. never log credentials. keys are made to be unique in the first 8 characters.
		MDC.put(MdcUtils.MDC_API_KEY, StringUtils.substring(apiKey, 0, 8));
	}
}

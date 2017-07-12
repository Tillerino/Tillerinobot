package tillerino.tillerinobot.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.slf4j.MDC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.RateLimiter;

/**
 * General logging for API. Also sets/logs rate limiter options.
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ApiLoggingFeature implements Feature {
	private final Before before;
	private final After after;

	@PreMatching
	public static class SetPath implements ContainerRequestFilter {
		@Override
		public void filter(ContainerRequestContext requestContext) throws IOException {
			MDC.put("apiPath", requestContext.getUriInfo().getPath());
		}
	}

	@Priority(Priorities.USER)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class Before implements ContainerRequestFilter {
		private final RateLimiter rateLimiter;

		@Override
		public void filter(ContainerRequestContext requestContext) {
			rateLimiter.setThreadPriority(RateLimiter.REQUEST);
			// clear blocked time in case it wasn't cleared by the last thread
			rateLimiter.blockedTime();
		}
	}

	@Priority(Priorities.USER)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class After implements ContainerResponseFilter {
		private final RateLimiter rateLimiter;

		@Override
		public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
			rateLimiter.clearThreadPriority();
			MDC.put(IRCBot.MCD_OSU_API_RATE_BLOCKED_TIME, String.valueOf(rateLimiter.blockedTime()));
			MDC.put("apiStatus", String.valueOf(responseContext.getStatus()));
			log.debug("API call finished");
			MDC.clear();
		}
	}

	@Override
	public boolean configure(FeatureContext context) {
		context.register(SetPath.class);
		context.register(before);
		context.register(after);
		return true;
	}
}

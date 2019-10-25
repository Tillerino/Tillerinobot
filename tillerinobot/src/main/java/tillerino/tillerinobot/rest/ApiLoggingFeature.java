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
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
			MDC.put(MdcUtils.MDC_API_PATH, requestContext.getUriInfo().getPath());
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
		@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "For the try-with. Looks like this is a Java 13 bug in Spotbugs 3.1.11")
		public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
			rateLimiter.clearThreadPriority();
			try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MCD_OSU_API_RATE_BLOCKED_TIME, rateLimiter.blockedTime())) {
				mdc.add(MdcUtils.MDC_API_STATUS, responseContext.getStatus());
				log.debug("API call finished");
			}
			MDC.clear(); // clear to remove path which is set in the pre filter
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

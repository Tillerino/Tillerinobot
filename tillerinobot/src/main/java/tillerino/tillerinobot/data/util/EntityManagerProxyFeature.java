package tillerino.tillerinobot.data.util;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Inject))
public class EntityManagerProxyFeature implements Feature {
	private static final String EMKEY = "A thread-local entity manager was set";

	private final SetEntityManagerProxy set;
	private final CloseEntityManagerProxy close;

	@Priority(0)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class SetEntityManagerProxy implements ContainerRequestFilter {
		private final EntityManagerFactory emf;
		private final ThreadLocalAutoCommittingEntityManager em;

		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			try {
				em.setThreadLocalEntityManager(emf.createEntityManager());
				requestContext.setProperty(EMKEY, true);
			} catch (Exception e) {
				log.error("Error setting entity manager", e);
				requestContext.removeProperty(EMKEY);
				throw new InternalServerErrorException(e);
			}
		}
	}

	@Priority(0)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class CloseEntityManagerProxy implements
			ContainerResponseFilter {
		private final ThreadLocalAutoCommittingEntityManager em;

		@Override
		public void filter(ContainerRequestContext requestContext,
				ContainerResponseContext responseContext) throws IOException {
			try {
				if (requestContext.getProperty(EMKEY) != null) {
					em.close();
				}
			} catch (Exception e) {
				log.error("Error closing entity manager", e);
			}
		}
	}

	@Override
	public boolean configure(FeatureContext context) {
		context.register(set);
		context.register(close);
		return true;
	}

}

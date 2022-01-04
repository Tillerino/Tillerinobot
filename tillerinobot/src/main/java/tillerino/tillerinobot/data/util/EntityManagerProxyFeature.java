package tillerino.tillerinobot.data.util;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Inject))
public class EntityManagerProxyFeature implements Feature {
	private static final String EMKEY = "A thread-local entity manager was set";

	private final SetEntityManagerProxy set;
	private final CloseEntityManagerProxy close;

	@Priority(0)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class SetEntityManagerProxy implements ContainerRequestFilter {
		private final ThreadLocalAutoCommittingEntityManager em;

		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			try {
				requestContext.setProperty(EMKEY, em.withNewEntityManager());
			} catch (Exception e) {
				log.error("Error setting entity manager", e);
				throw new InternalServerErrorException(e);
			}
		}
	}

	@Priority(0)
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class CloseEntityManagerProxy implements
			ContainerResponseFilter {
		@Override
		public void filter(ContainerRequestContext requestContext,
				ContainerResponseContext responseContext) throws IOException {
			try {
				ResetEntityManagerCloseable closeable = (ResetEntityManagerCloseable) requestContext.getProperty(EMKEY);
				if (closeable != null) {
					closeable.close();
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

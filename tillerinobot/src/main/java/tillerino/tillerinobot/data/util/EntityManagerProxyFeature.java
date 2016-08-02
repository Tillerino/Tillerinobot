package tillerino.tillerinobot.data.util;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class EntityManagerProxyFeature implements Feature {
	private final SetEntityManagerProxy set;
	private final CloseEntityManagerProxy close;

	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class SetEntityManagerProxy implements ContainerRequestFilter {
		private final EntityManagerFactory emf;
		private final ThreadLocalAutoCommittingEntityManager em;

		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			System.out.println("AAAAAAAAAAHHHHHHHHHH");
			em.setThreadLocalEntityManager(emf.createEntityManager());
		}
	}

	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	public static class CloseEntityManagerProxy implements
			ContainerResponseFilter {
		private final ThreadLocalAutoCommittingEntityManager em;

		@Override
		public void filter(ContainerRequestContext requestContext,
				ContainerResponseContext responseContext) throws IOException {
			em.close();
		}
	}

	@Override
	public boolean configure(FeatureContext context) {
		context.register(set);
		context.register(close);
		return true;
	}

}

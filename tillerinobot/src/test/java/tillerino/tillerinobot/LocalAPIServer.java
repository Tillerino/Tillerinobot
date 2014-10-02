package tillerino.tillerinobot;

import static org.mockito.Mockito.mock;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class LocalAPIServer extends AbstractModule {
	@Override
	protected void configure() {
		bind(BotRunner.class).toInstance(mock(BotRunner.class));

		bind(BotBackend.class).to(TestBackend.class);
		bind(Boolean.class).annotatedWith(
				Names.named("tillerinobot.test.persistentBackend")).toInstance(
				false);
	}
	
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new LocalAPIServer());
		
		URI baseUri = UriBuilder.fromUri("http://localhost/").port(1666).build();
		ResourceConfig config = ResourceConfig.forApplication(injector.getInstance(BotAPIServer.class));
		Server server = JettyHttpContainerFactory.createServer(baseUri, config);
		((QueuedThreadPool) server.getThreadPool()).setMaxThreads(32);
		
		server.start();
	}
}

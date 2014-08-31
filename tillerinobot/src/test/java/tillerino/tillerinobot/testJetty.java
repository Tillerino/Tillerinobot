package tillerino.tillerinobot;

import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class testJetty {
	@Test
	public void test() throws Exception {
		Server server = new Server(666);
		
		server.setHandler(new BotAPIServer());
		
		server.start();
		server.join();
	}
}

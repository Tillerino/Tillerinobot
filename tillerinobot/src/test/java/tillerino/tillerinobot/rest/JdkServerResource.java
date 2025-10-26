package tillerino.tillerinobot.rest;

import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.core.Application;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
@RequiredArgsConstructor
public class JdkServerResource implements BeforeEachCallback, AfterEachCallback {
    private final Application app;

    private final String host;

    private final int port;

    private int actualPort = 0;

    private HttpServer server;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        server = JdkHttpServerFactory.createHttpServer(
                new URI("http", null, host, port, "/", null, null), ResourceConfig.forApplication(app));
        actualPort = server.getAddress().getPort();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            server.stop(1);
        } catch (Exception e) {
            log.error("Stopping Jetty failed", e);
        }
    }

    public int getPort() {
        return actualPort;
    }
}

package tillerino.tillerinobot.rest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;

public final class RestUtils {
    private RestUtils() {
        // utils class
    }

    public static WebApplicationException getBadGateway(IOException e) {
        return new WebApplicationException(
                e != null ? e.getMessage() : "Communication with the osu API server failed.",
                Status.fromStatusCode(502));
    }

    public static WebApplicationException getInterrupted() {
        return new WebApplicationException("The server is being shutdown for maintenance", Status.SERVICE_UNAVAILABLE);
    }

    @SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
    public static Throwable refreshWebApplicationException(Throwable t) {
        if (t instanceof WebApplicationException web) {
            return new WebApplicationException(
                    t.getCause(), Response.fromResponse(web.getResponse()).build());
        }
        return t;
    }
}

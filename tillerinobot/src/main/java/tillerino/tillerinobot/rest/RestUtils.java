package tillerino.tillerinobot.rest;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public final class RestUtils {
	private RestUtils() {
		// utils class
	}

	public static WebApplicationException getBadGateway(IOException e) {
		return new WebApplicationException(e != null ? e.getMessage() : "Communication with the osu API server failed.", Status.fromStatusCode(502));
	}

	public static WebApplicationException getInterrupted() {
		return new WebApplicationException("The server is being shutdown for maintenance", Status.SERVICE_UNAVAILABLE);
	}

	public static Throwable refreshWebApplicationException(Throwable t) {
		if (t instanceof WebApplicationException) {
			return new WebApplicationException(t.getCause(), Response.fromResponse(((WebApplicationException) t).getResponse()).build());
		}
		return t;
	}

}

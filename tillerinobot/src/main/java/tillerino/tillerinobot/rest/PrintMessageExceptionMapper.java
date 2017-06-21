package tillerino.tillerinobot.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PrintMessageExceptionMapper implements ExceptionMapper<WebApplicationException> {
	@Override
	public Response toResponse(WebApplicationException exception) {
		Response response = exception.getResponse();
		if (!response.hasEntity() && exception.getMessage() != null) {
			return Response.fromResponse(response).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		return response;
	}
}

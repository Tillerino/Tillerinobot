package org.tillerino.ppaddict.rest;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service for API key authentication.
 */
@Path("/keys")
public interface AuthenticationService {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Authorization {
		private boolean admin;
	}

	/**
	 * Finds the authorization for a given API key.
	 *
	 * @throws NotFoundException if there is no such key.
	 */
	@GET
	@Path("/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	Authorization findKey(@PathParam("key") String key) throws NotFoundException;
}

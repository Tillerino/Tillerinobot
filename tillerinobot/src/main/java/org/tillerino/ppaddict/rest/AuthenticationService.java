package org.tillerino.ppaddict.rest;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

	/**
	 * Creates a new API key for an osu user.
	 *
	 * @param adminKey admin key of the application. This key must be authorized for key creation.
	 * @param osuUserId id of the user to create an API key for. Any existing key is revoked.
	 * @return the new API key
	 */
	String createKey(String adminKey, int osuUserId) throws NotFoundException, ForbiddenException;
}

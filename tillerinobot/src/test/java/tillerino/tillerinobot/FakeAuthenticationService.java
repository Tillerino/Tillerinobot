package tillerino.tillerinobot;

import java.util.UUID;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.tillerino.ppaddict.rest.AuthenticationService;

public class FakeAuthenticationService implements AuthenticationService {
	@Override
	public Authorization findKey(String key) throws NotFoundException {
		if (key.equals("testKey") || key.equals("valid-key")) {
			return new Authorization(false);
		}
		throw new NotFoundException();
	}

	@Override
	public String createKey(String adminKey, int osuUserId) throws NotFoundException, ForbiddenException {
		return UUID.randomUUID().toString(); // not quite the usual format, but meh
	}
}
package tillerino.tillerinobot;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NoArgsConstructor;
import org.tillerino.ppaddict.rest.AuthenticationService;

@Singleton
@NoArgsConstructor(onConstructor_ = @Inject)
public class FakeAuthenticationService implements AuthenticationService {
    @Override
    public Authorization getAuthorization(String key) throws NotFoundException {
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

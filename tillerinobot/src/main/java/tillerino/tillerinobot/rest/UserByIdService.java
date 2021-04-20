package tillerino.tillerinobot.rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.IrcNameResolver;

@Singleton
@Path("/userbyid")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserByIdService {
    private final IrcNameResolver resolver;

    @KeyRequired
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OsuApiUser getUserById(@QueryParam("id") @UserId int id) throws SQLException {
        try {
            OsuApiUser user = resolver.resolveManually(id);
            if (user == null) {
                throw new NotFoundException("user with that id does not exist");
            } else {
                return user;
            }
        } catch (IOException e) {
            throw RestUtils.getBadGateway(null);
        }
    }
}

package tillerino.tillerinobot.rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BotAPIServer;
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
            throw BotAPIServer.getBadGateway(null);
        }
    }
}

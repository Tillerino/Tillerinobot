package tillerino.tillerinobot.rest;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;
import tillerino.tillerinobot.BotAPIServer;
import tillerino.tillerinobot.BotBackend;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;

@Singleton
@Path("/userbyid")
public class UserByIdService {
    private final BotBackend backend;

    @Inject
    public UserByIdService(BotBackend server) {
        this.backend = server;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OsuApiUser getUserById(@QueryParam("k") String key, @QueryParam("id") @UserId int id) throws SQLException {
        BotAPIServer.throwUnautorized(backend.verifyGeneralKey(key));
        try {
            OsuApiUser user = backend.resolveManually(id);
            if (user == null) {
                throw BotAPIServer.getNotFound("user with that id does not exist");
            } else {
                return user;
            }
        } catch (IOException e) {
            throw BotAPIServer.getBadGateway(null);
        }
    }
}

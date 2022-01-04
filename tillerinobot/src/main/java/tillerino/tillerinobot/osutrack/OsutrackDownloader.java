package tillerino.tillerinobot.osutrack;

import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

public class OsutrackDownloader {
    //  __________________________________________________________________________________________
    // /\                                                                                         \
    // \_|    If you're reading this and thinking, hey, I wanna use that osutrack API as well:    |
    //   |    --> PLEASE ask FIRST for permission from ameo (https://ameobea.me/osutrack/) <--    |
    //   |   _____________________________________________________________________________________|_
    //    \_/_______________________________________________________________________________________/
    private static final String OSUTRACK_ENDPOINT = "https://osutrack-api.ameo.dev/update";
    private static final OsutrackUpdate OSUTRACK_UPDATE = WebResourceFactory.newResource(OsutrackUpdate.class, ClientBuilder
            .newClient()
            .target(OSUTRACK_ENDPOINT)
            .queryParam("mode", 0));

    protected void completeUpdateObject(UpdateResult updateResult) {
        for (Highscore highscore : updateResult.getNewHighscores()) {
            highscore.setMode(0);
        }
    }

    public UpdateResult getUpdate(int osuUserId) {
        UpdateResult updateResult = OSUTRACK_UPDATE.getUpdate(osuUserId);
        completeUpdateObject(updateResult);
        return updateResult;
    }
}

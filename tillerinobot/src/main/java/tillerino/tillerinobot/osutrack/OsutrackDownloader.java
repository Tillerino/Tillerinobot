package tillerino.tillerinobot.osutrack;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OsutrackDownloader {
    //  __________________________________________________________________________________________
    // /\                                                                                         \
    // \_|    If you're reading this and thinking, hey, I wanna use that osutrack API as well:    |
    //   |    --> PLEASE ask FIRST for permission from ameo (https://ameobea.me/osutrack/) <--    |
    //   |   _____________________________________________________________________________________|_
    //    \_/_______________________________________________________________________________________/
    private static final String OSUTRACK_ENDPOINT = "https://ameobea.me/osutrack/api/get_changes.php?user=%s&mode=0";

    static final ObjectMapper JACKSON = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    protected UpdateResult parseJson(String json) {
        UpdateResult updateResult;
        try {
            updateResult = JACKSON.readValue(json, UpdateResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Highscore highscore : updateResult.getNewHighscores()) {
            highscore.setMode(0);
        }
        return updateResult;
    }

    public UpdateResult getUpdate(String username) throws IOException {
        URL endpoint = new URL(String.format(OSUTRACK_ENDPOINT, username.replace(' ', '_')));

        // lets shamefully reuse osuApiConnector downloader :D:D
        String json = org.tillerino.osuApiModel.Downloader.downloadDirect(endpoint, 30000);

        return parseJson(json);
    }
}

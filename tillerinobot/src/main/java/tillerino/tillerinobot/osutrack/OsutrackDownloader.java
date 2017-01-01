package tillerino.tillerinobot.osutrack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;

public class OsutrackDownloader {
    //  __________________________________________________________________________________________
    // /\                                                                                         \
    // \_|    If you're reading this and thinking, hey, I wanna use that osutrack API as well:    |
    //   |    --> PLEASE ask FIRST for permission from ameo (https://ameobea.me/osutrack/) <--    |
    //   |   _____________________________________________________________________________________|_
    //    \_/_______________________________________________________________________________________/
    private static final String OSUTRACK_ENDPOINT = "https://ameobea.me/osutrack/api/get_changes.php?user=%s&mode=0";

    final Gson gson;

    public OsutrackDownloader() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Highscore.class, new HighscoreAdapter())
                .create();
    }

    public UpdateResult getUpdate(String username) throws IOException {
        URL endpoint = new URL(String.format(OSUTRACK_ENDPOINT, username.replace(' ', '_')));

        // lets shamefully reuse osuApiConnector downloader :D:D
        String json = org.tillerino.osuApiModel.Downloader.downloadDirect(endpoint);

        return gson.fromJson(json, UpdateResult.class);
    }
}

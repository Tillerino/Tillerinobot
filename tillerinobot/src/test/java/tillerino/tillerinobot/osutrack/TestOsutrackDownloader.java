package tillerino.tillerinobot.osutrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TestOsutrackDownloader extends OsutrackDownloader {
    @Override
    public UpdateResult getUpdate(String username) throws IOException {
        InputStream inputStream = TestOsutrackDownloader.class.getResourceAsStream("/osutrack/" + username.replace(' ', '_') + ".json");
        String json = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .parallel().collect(Collectors.joining("\n"));
        return parseJson(json);
    }
}

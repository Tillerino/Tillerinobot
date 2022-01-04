package tillerino.tillerinobot.osutrack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TestOsutrackDownloader extends OsutrackDownloader {
    static final ObjectMapper JACKSON = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    protected UpdateResult parseJson(String json) {
        UpdateResult updateResult;
        try {
            updateResult = JACKSON.readValue(json, UpdateResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        completeUpdateObject(updateResult);
        return updateResult;
    }


    @Override
    public UpdateResult getUpdate(int osuUserId) {
        InputStream inputStream = TestOsutrackDownloader.class.getResourceAsStream("/osutrack/" + osuUserId + ".json");
        String json = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        return parseJson(json);
    }
}

package tillerino.tillerinobot.osutrack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Binds;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
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
        if (inputStream == null) {
            throw new NotFoundException();
        }
        String json = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        return parseJson(json);
    }

    @dagger.Module
    public interface Module {
        @Binds
        OsutrackDownloader osutrackDownloader(TestOsutrackDownloader testOsutrackDownloader);
    }
}

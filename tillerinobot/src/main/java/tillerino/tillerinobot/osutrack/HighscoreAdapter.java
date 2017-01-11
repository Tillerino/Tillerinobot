package tillerino.tillerinobot.osutrack;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.deserializer.CustomGson;

import java.io.IOException;

public class HighscoreAdapter extends TypeAdapter<Highscore> {
    private final Gson gson = CustomGson.wrap(false, Highscore.class);
    private final JsonParser jsonParser = new JsonParser();

    @Override
    public void write(JsonWriter out, Highscore highscore) throws IOException {
        if(highscore == null) {
            out.nullValue();
        } else {
            gson.toJson(highscore, Highscore.class, out);
        }
    }

    @Override
    public Highscore read(JsonReader in) throws IOException {
        JsonElement e = jsonParser.parse(in);

        if(e.isJsonNull()) {
            return null;
        }

        if(e.isJsonObject()) {
            return OsuApiScore.fromJsonObject((JsonObject) e, Highscore.class, 0);
        }

        throw new IllegalStateException("Json element was not 'null' or 'object'");
    }
}

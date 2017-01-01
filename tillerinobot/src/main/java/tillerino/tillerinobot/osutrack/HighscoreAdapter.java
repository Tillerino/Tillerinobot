package tillerino.tillerinobot.osutrack;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.deserializer.CustomGson;

import java.io.IOException;

public class HighscoreAdapter extends TypeAdapter<Highscore> {
    private final Gson gson = CustomGson.wrap(false, Highscore.class);

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
        JsonToken peek = in.peek();
        switch (peek) {
            case NULL:
                in.nextNull();
                return null;
            case BEGIN_OBJECT:
                break;
            default:
                throw new IllegalStateException("Expected NULL or BEGIN_OBJECT but was " + peek);
        }

        JsonObject o = (JsonObject)new JsonParser().parse(in);
        return OsuApiScore.fromJsonObject(o, Highscore.class, 0);
    }
}

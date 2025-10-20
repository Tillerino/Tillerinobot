package tillerino.tillerinobot;

import java.io.IOException;
import java.util.List;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;

/**
 * Abstraction to switch between different osu! API implementations, especially v1 and v2.
 *
 * <p>All methods return our database objects. These only contain the additional "downloaded" field, which is
 * initialized to right-now, so setting the properties already contained in the {@code org.tillerino.osuApiModel.*}
 * classes is sufficient.
 */
public interface OsuApi {
    ApiUser getUser(@UserId int userId, @GameMode int gameMode) throws IOException;

    ApiUser getUser(String username, @GameMode int mode) throws IOException;

    ApiBeatmap getBeatmap(@BeatmapId int beatmapid, @BitwiseMods long mods) throws IOException;

    List<ApiScore> getUserTop(@UserId int userId, @GameMode int mode, int limit) throws IOException;

    List<ApiScore> getUserRecent(@UserId int userid, @GameMode int mode) throws IOException;
}

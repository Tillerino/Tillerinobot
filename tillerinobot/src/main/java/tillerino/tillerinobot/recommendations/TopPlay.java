package tillerino.tillerinobot.recommendations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopPlay {
    @UserId
    private int userid;

    private int place;

    @BeatmapId
    private int beatmapid;

    @BitwiseMods
    private long mods;

    private double pp;

    public BeatmapWithMods idAndMods() {
        return new BeatmapWithMods(beatmapid, mods);
    }
}

package tillerino.tillerinobot.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.UserId;

@NoArgsConstructor
@AllArgsConstructor
@Table("usertop50")
public class UserTop50Entry {
    @UserId
    public int userid;

    public int place;

    @BeatmapId
    public int beatmapid;
}

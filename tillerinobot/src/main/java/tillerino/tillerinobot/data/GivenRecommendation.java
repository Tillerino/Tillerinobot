package tillerino.tillerinobot.data;

import lombok.Data;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

@Table("givenrecommendations")
@KeyColumn("id")
@Data
public class GivenRecommendation {
    public GivenRecommendation(@UserId int userid, @BeatmapId int beatmapid, long date, @BitwiseMods long mods) {
        super();
        this.userid = userid;
        this.beatmapid = beatmapid;
        this.date = date;
        this.mods = mods;
    }

    public GivenRecommendation() {}

    private Integer id;

    @UserId
    private int userid;

    @BeatmapId
    private int beatmapid;

    private long date;

    @BitwiseMods
    public long mods;

    /** If true, this won't be taken into consideration when generating recommendations. */
    private boolean forgotten = false;
    /** If true, this won't be displayed in the recommendations list in ppaddict anymore. */
    private boolean hidden = false;
}

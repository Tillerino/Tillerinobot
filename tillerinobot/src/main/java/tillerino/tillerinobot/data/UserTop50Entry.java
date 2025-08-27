package tillerino.tillerinobot.data;

import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.UserId;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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

package tillerino.tillerinobot.recommendations;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

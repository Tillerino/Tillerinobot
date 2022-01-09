package tillerino.tillerinobot.recommendations;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import lombok.Data;

@Data
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

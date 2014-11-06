package tillerino.tillerinobot.predicates;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

public interface NumericBeatmapProperty {
	String getName();

	double getValue(OsuApiBeatmap beatmap, @BitwiseMods long mods);
}
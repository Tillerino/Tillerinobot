package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;

import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class OverallDifficulty implements NumericBeatmapProperty {

	@Override
	public String getName() {
		return "OD";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getOverallDifficulty(mods);
	}

}

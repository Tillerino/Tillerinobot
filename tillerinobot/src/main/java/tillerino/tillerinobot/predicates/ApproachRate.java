package tillerino.tillerinobot.predicates;

import org.tillerino.osuApiModel.OsuApiBeatmap;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ApproachRate implements NumericBeatmapProperty {
	@Override
	public String getName() {
		return "AR";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getApproachRate(mods);
	}
}

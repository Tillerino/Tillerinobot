package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;

import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class BeatsPerMinute implements NumericBeatmapProperty {

	@Override
	public String getName() {
		return "BPM";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getBpm(mods);
	}

}

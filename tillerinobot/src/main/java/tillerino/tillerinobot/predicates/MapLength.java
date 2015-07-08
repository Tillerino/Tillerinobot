package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;

import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class MapLength implements NumericBeatmapProperty {

	@Override
	public String getName() {
		return "LEN";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getTotalLength(mods);
	}

}

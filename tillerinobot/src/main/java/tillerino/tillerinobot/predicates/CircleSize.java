package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class CircleSize implements NumericBeatmapProperty {

	@Override
	public String getName() {
		return "CS";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getCircleSize(mods);
	}

}

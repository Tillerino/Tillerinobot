package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;

import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class TitleLength implements NumericBeatmapProperty {
	@Override
	public String getName() {
		return "TL";
	}

	@Override
	public double getValue(OsuApiBeatmap beatmap, long mods) {
		return beatmap.getTitle().length() * (mods != 0l ? 2 : 1);
	}
}
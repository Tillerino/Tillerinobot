package tillerino.tillerinobot.predicates;

import org.tillerino.osuApiModel.OsuApiBeatmap;

public class StarDiff implements NumericBeatmapProperty {
    @Override
    public String getName() {
        return "STAR";
    }

    @Override
    public double getValue(OsuApiBeatmap beatmap, long mods) {
        return beatmap.getStarDifficulty();
    }
}

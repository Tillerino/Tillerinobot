package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class OverallDifficulty implements NumericBeatmapProperty {

    @Override
    public String getName() {
        return "OD";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getOverallDifficulty(mods);
    }
}

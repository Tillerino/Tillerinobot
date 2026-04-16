package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class ApproachRate implements NumericBeatmapProperty {
    @Override
    public String getName() {
        return "AR";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getApproachRate(mods);
    }
}

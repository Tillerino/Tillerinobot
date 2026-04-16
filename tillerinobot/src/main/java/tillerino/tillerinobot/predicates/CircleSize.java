package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class CircleSize implements NumericBeatmapProperty {

    @Override
    public String getName() {
        return "CS";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getCircleSize(mods);
    }
}

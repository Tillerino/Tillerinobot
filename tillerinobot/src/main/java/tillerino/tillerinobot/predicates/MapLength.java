package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class MapLength implements NumericBeatmapProperty {

    @Override
    public String getName() {
        return "LEN";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getTotalLength(mods);
    }
}

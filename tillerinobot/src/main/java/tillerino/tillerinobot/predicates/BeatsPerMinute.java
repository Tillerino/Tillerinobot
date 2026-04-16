package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class BeatsPerMinute implements NumericBeatmapProperty {

    @Override
    public String getName() {
        return "BPM";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getBpm(mods);
    }
}

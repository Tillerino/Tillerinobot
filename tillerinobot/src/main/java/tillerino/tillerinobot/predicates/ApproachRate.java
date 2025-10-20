package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import org.tillerino.osuApiModel.OsuApiBeatmap;

@EqualsAndHashCode
public class ApproachRate implements NumericBeatmapProperty {
    @Override
    public String getName() {
        return "AR";
    }

    @Override
    public double getValue(OsuApiBeatmap beatmap, long mods) {
        return beatmap.getApproachRate(mods);
    }
}

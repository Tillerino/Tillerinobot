package tillerino.tillerinobot.predicates;

import lombok.EqualsAndHashCode;
import tillerino.tillerinobot.data.ApiBeatmap;

@EqualsAndHashCode
public class TitleLength implements NumericBeatmapProperty {
    @Override
    public String getName() {
        return "TL";
    }

    @Override
    public double getValue(ApiBeatmap beatmap, long mods) {
        return beatmap.getTitle().length() * (mods != 0L ? 2 : 1);
    }
}

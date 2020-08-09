package tillerino.tillerinobot.diff;

import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * A reduced version of {@link OsuApiBeatmap} which contains just the fields
 * necessary for difficulty calculation.
 * This serves to reduce memory usage, since we potentially keep a lot of these
 * in memory.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OsuApiBeatmapForDiff {
	int maxCombo, approved;
	double aimDifficulty, speedDifficulty, starDifficulty, overallDifficulty, approachRate;

	public double getOverallDifficulty(@BitwiseMods long mods) {
		return OsuApiBeatmap.calcOd(getOverallDifficulty(), mods);
	}

	public double getApproachRate(@BitwiseMods long mods) {
		return OsuApiBeatmap.calcAR(getApproachRate(), mods);
	}

	@org.mapstruct.Mapper
	public interface Mapper {
		Mapper INSTANCE = Mappers.getMapper(Mapper.class);

		OsuApiBeatmapForDiff shrink(OsuApiBeatmap large);
	}
}

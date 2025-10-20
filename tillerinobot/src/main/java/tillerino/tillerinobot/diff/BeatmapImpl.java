package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;

/**
 * Difficulty attributes from san-doku required for pp calc.
 */
@Builder
public record BeatmapImpl(
		@BitwiseMods long modsUsed,
		double OverallDifficulty,
		double ApproachRate,
		int HitCircleCount,
		int SliderCount,
		int SpinnerCount,
		double StarDiff,
		int MaxCombo,
		double AimDifficulty,
		double AimDifficultSliderCount,
		double SpeedDifficulty,
		double SpeedNoteCount,
		double SliderFactor,
		double AimDifficultStrainCount,
		double SpeedDifficultStrainCount,
		double FlashlightDifficulty) {


	public int getObjectCount() {
		return HitCircleCount + SliderCount + SpinnerCount;
	}
}

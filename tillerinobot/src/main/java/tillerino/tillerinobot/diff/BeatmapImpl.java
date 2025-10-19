package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;

/**
 * Difficulty attributes from san-doku required for pp calc.
 */
@Builder
// suppress warning about case-insensitive field collision, because we cannot change the names in CBeatmap
@SuppressWarnings("squid:S1845")
@SuppressFBWarnings("NM")
public record BeatmapImpl(
		@BitwiseMods long modsUsed,
		float OverallDifficulty,
		float ApproachRate,
		int HitCircleCount,
		int SliderCount,
		int SpinnerCount,
		float StarDiff,
		int MaxCombo,
		float AimDifficulty,
		float AimDifficultSliderCount,
		float SpeedDifficulty,
		float SpeedNoteCount,
		float SliderFactor,
		float AimDifficultStrainCount,
		float SpeedDifficultStrainCount,
		float FlashlightDifficulty) {


	public int getObjectCount() {
		return HitCircleCount + SliderCount + SpinnerCount;
	}
}

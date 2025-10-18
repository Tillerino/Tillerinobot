package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;

/**
 * This class implements {@link Beatmap} which is the interface that the
 * translated pp calculation uses to get info about the beatmap.
 */
@Builder
// suppress warning about case-insensitive field collision, because we cannot change the names in CBeatmap
@SuppressWarnings("squid:S1845")
@SuppressFBWarnings("NM")
public record BeatmapImpl(
		@BitwiseMods long modsUsed,
		float OverallDifficulty,
		float approachRate,
		int circleCount,
		int sliderCount,
		int spinnerCount,
		float starDiff,
		int MaxCombo,
		float AimDifficulty,
		float AimDifficultySliderCount,
		float SpeedDifficulty,
		float SpeedNoteCount,
		float SliderFactor,
		float AimDifficultyStrainCount,
		float SpeedDifficultyStrainCount,
		float flashlight) implements Beatmap {
	

	@Override
	public float DifficultyAttribute(long mods, int kind) {
		if (Beatmap.getDiffMods(mods) != modsUsed) {
			throw new IllegalArgumentException("Unexpected mods " + mods + ". Was loaded with " + modsUsed);
		}

		return switch (kind) {
			case Beatmap.Aim -> AimDifficulty;
			case Beatmap.Speed -> SpeedDifficulty;
			case Beatmap.OD -> OverallDifficulty;
			case Beatmap.AR -> approachRate;
			case Beatmap.MaxCombo -> MaxCombo;
			case Beatmap.SliderFactor -> SliderFactor;
			case Beatmap.Flashlight -> flashlight;
			case Beatmap.SpeedNoteCount -> SpeedNoteCount;
			case Beatmap.AimDifficultStrainCount -> AimDifficultyStrainCount;
			case Beatmap.SpeedDifficultStrainCount -> SpeedDifficultyStrainCount;
			default -> throw new IllegalArgumentException("Unexpected kind: " + kind);
		};
	}

	@Override
	public int HitCircleCount() {
		return circleCount;
	}

	@Override
	public int SpinnerCount() {
		return spinnerCount;
	}

	@Override
	public int SliderCount() {
		return sliderCount;
	}

	public int getObjectCount() {
		return circleCount + sliderCount + spinnerCount;
	}
}

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
		float starDiff,
		float aim,
		float speed,
		float overallDifficulty,
		float approachRate,
		int maxCombo,
		float sliderFactor,
		float flashlight,
		float speedNoteCount,
		int circleCount,
		int spinnerCount,
		int sliderCount,
		float aimDifficultyStrainCount,
		float speedDifficultyStrainCount) implements Beatmap {
	

	@Override
	public float DifficultyAttribute(long mods, int kind) {
		if (Beatmap.getDiffMods(mods) != modsUsed) {
			throw new IllegalArgumentException("Unexpected mods " + mods + ". Was loaded with " + modsUsed);
		}

		return switch (kind) {
			case Beatmap.Aim -> aim;
			case Beatmap.Speed -> speed;
			case Beatmap.OD -> overallDifficulty;
			case Beatmap.AR -> approachRate;
			case Beatmap.MaxCombo -> maxCombo;
			case Beatmap.SliderFactor -> sliderFactor;
			case Beatmap.Flashlight -> flashlight;
			case Beatmap.SpeedNoteCount -> speedNoteCount;
			case Beatmap.AimDifficultStrainCount -> aimDifficultyStrainCount;
			case Beatmap.SpeedDifficultStrainCount -> speedDifficultyStrainCount;
			default -> throw new IllegalArgumentException("Unexpected kind: " + kind);
		};
	}

	@Override
	public int NumHitCircles() {
		return circleCount;
	}

	@Override
	public int NumSpinners() {
		return spinnerCount;
	}

	@Override
	public int NumSliders() {
		return sliderCount;
	}

	public int getObjectCount() {
		return circleCount + sliderCount + spinnerCount;
	}
}
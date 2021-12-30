package tillerino.tillerinobot.diff.sandoku;

import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import tillerino.tillerinobot.diff.Beatmap;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SanDokuResponse {
	private @GameMode int beatmapGameMode;
	private @GameMode int gameModeUsed;
	private @BitwiseMods long modsUsed;
	private SanDokuDiffCalcResult diffCalcResult;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SanDokuDiffCalcResult {
		// removed fields which are not required for standard
		private double starRating;
		private int maxCombo;
		private double aimStrain;
		private double speedStrain;
		private double flashlightRating;
		private double sliderFactor;
		private double approachRate;
		private double overallDifficulty;
		private int hitCircleCount;
		private int sliderCount;
		private int spinnerCount;
	}

	public Beatmap toBeatmap() {
		return new Beatmap() {
			@Override
			public float DifficultyAttribute(long mods, int kind) {
				if (Beatmap.getDiffMods(mods) != modsUsed) {
					throw new IllegalArgumentException("Unexpected mods " + mods + ". Was loaded with " + modsUsed);
				}

				return switch (kind) {
					case Beatmap.Aim -> (float) diffCalcResult.aimStrain;
					case Beatmap.AR -> (float) diffCalcResult.approachRate;
					case Beatmap.Flashlight -> (float) diffCalcResult.flashlightRating;
					case Beatmap.MaxCombo -> diffCalcResult.maxCombo;
					case Beatmap.OD -> (float) diffCalcResult.overallDifficulty;
					case Beatmap.SliderFactor -> (float) diffCalcResult.sliderFactor;
					case Beatmap.Speed -> (float) diffCalcResult.speedStrain;
					default -> throw new IllegalArgumentException("Unexpected kind: " + kind);
				};
			}

			@Override
			public int NumHitCircles() {
				return diffCalcResult.hitCircleCount;
			}

			@Override
			public int NumSpinners() {
				return diffCalcResult.spinnerCount;
			}

			@Override
			public int NumSliders() {
				return diffCalcResult.sliderCount;
			}
		};
	}
}

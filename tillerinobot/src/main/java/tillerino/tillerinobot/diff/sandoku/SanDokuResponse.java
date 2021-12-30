package tillerino.tillerinobot.diff.sandoku;

import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import tillerino.tillerinobot.diff.BeatmapImpl;

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

	public BeatmapImpl toBeatmap() {
		return BeatmapImpl.builder()
				.aim((float) diffCalcResult.aimStrain)
				.approachRate((float) diffCalcResult.approachRate)
				.circleCount(diffCalcResult.hitCircleCount)
				.flashlight((float) diffCalcResult.flashlightRating)
				.maxCombo(diffCalcResult.maxCombo)
				.modsUsed(modsUsed)
				.overallDifficulty((float) diffCalcResult.overallDifficulty)
				.sliderCount(diffCalcResult.sliderCount)
				.speed((float) diffCalcResult.speedStrain)
				.spinnerCount(diffCalcResult.spinnerCount)
				.starDiff((float) diffCalcResult.starRating)
				.build();
	}
}

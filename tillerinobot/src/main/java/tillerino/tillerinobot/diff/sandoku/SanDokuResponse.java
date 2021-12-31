package tillerino.tillerinobot.diff.sandoku;

import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import tillerino.tillerinobot.diff.BeatmapImpl;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SanDokuResponse(
		@GameMode int beatmapGameMode,
		@GameMode int gameModeUsed,
		@BitwiseMods long modsUsed,
		SanDokuDiffCalcResult diffCalcResult) {

	@Builder(toBuilder = true)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static record SanDokuDiffCalcResult(
		// removed fields which are not required for standard
		double starRating,
		int maxCombo,
		double aimStrain,
		double speedStrain,
		double flashlightRating,
		double sliderFactor,
		double approachRate,
		double overallDifficulty,
		int hitCircleCount,
		int sliderCount,
		int spinnerCount) {
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

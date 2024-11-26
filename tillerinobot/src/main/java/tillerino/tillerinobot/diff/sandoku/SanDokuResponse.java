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
		String beatmapMd5,
		@GameMode int gameModeUsed,
		@BitwiseMods long modsUsed,
		SanDokuDiffCalcResult diffCalcResult) {

	@Builder(toBuilder = true)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SanDokuDiffCalcResult(
		// only declare fields which are needed for std calc
		double starRating,
		int maxCombo,
		double aim,
		double speed,
		double speedNoteCount,
		double flashlight,
		double sliderFactor,
		double aimDifficultStrainCount,
		double speedDifficultStrainCount,
		double approachRate,
		double overallDifficulty,
		double drainRate,
		int hitCircleCount,
		int sliderCount,
		int spinnerCount) {
	}

	public BeatmapImpl toBeatmap() {
		return BeatmapImpl.builder()
				.modsUsed(modsUsed)
				.starDiff((float) diffCalcResult.starRating)
				.aim((float) diffCalcResult.aim)
				.speed((float) diffCalcResult.speed)
				.overallDifficulty((float) diffCalcResult.overallDifficulty)
				.approachRate((float) diffCalcResult.approachRate)
				.maxCombo(diffCalcResult.maxCombo)
				.sliderFactor((float) diffCalcResult.sliderFactor)
				.flashlight((float) diffCalcResult.flashlight)
				.speedNoteCount((float) diffCalcResult.speedNoteCount)
				.circleCount(diffCalcResult.hitCircleCount)
				.spinnerCount(diffCalcResult.spinnerCount)
				.sliderCount(diffCalcResult.sliderCount)
				.build();
	}
}

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
		// only declare fields which are needed for std calc
		int maxCombo,
		double starRating,
		double aim,
		double speed,
		double overallDifficulty,
		double approachRate,
		double flashlight,
		double sliderFactor,
		double speedNoteCount,
		int hitCircleCount,
		int sliderCount,
		int spinnerCount) {
	}

	public BeatmapImpl toBeatmap() {
		return BeatmapImpl.builder()
				.modsUsed(modsUsed)
				.starDiff((float) diffCalcResult.starRating)
				.AimDifficulty((float) diffCalcResult.aim)
				.SpeedDifficulty((float) diffCalcResult.speed)
				.OverallDifficulty((float) diffCalcResult.overallDifficulty)
				.approachRate((float) diffCalcResult.approachRate)
				.MaxCombo(diffCalcResult.maxCombo)
				.SliderFactor((float) diffCalcResult.sliderFactor)
				.flashlight((float) diffCalcResult.flashlight)
				.SpeedNoteCount((float) diffCalcResult.speedNoteCount)
				.circleCount(diffCalcResult.hitCircleCount)
				.spinnerCount(diffCalcResult.spinnerCount)
				.sliderCount(diffCalcResult.sliderCount)
				.build();
	}
}

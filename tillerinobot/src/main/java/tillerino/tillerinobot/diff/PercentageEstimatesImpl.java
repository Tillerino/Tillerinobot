package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Getter;
import tillerino.tillerinobot.UserException;

public class PercentageEstimatesImpl implements PercentageEstimates {
	private final BeatmapImpl beatmap;

	@Getter
	private final @BitwiseMods long mods;

	public PercentageEstimatesImpl(BeatmapImpl beatmap, @BitwiseMods long mods) {
		this.beatmap = beatmap;
		this.mods = mods;
	}

	@Override
	public double getPP(double acc) {
		AccuracyDistribution dist;
		try {
			dist = AccuracyDistribution.model(beatmap.getObjectCount(), 0, acc);
		} catch (UserException e) {
			// this should have been allowed to get here.
			throw new RuntimeException(e);
		}

		OsuScore score = new OsuScore((int) beatmap.DifficultyAttribute(getMods(), Beatmap.MaxCombo),
				dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(), getMods(), true);

		return score.getPP(beatmap);
	}

	@Override
	public double getPP(double acc, int combo, int misses) throws UserException {
		AccuracyDistribution dist = AccuracyDistribution.model(beatmap.getObjectCount(), misses, acc);

		OsuScore score = new OsuScore(combo, dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(),
				getMods(), true);

		return score.getPP(beatmap);
	}

	@Override
	public double getPP(int x100, int x50, int combo, int misses) {
		int x300 = beatmap.getObjectCount() - x50 - x100;
		OsuScore score = new OsuScore(combo, x300, x100, x50, misses, getMods(), true);

		return score.getPP(beatmap);
	}

	@Override
	public double getStarDiff() {
		return beatmap.starDiff();
	}

	@Override
	public double getApproachRate() {
		return beatmap.approachRate();
	}

	@Override
	public double getOverallDifficulty() {
		return beatmap.overallDifficulty();
	}
}
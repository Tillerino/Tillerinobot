package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.OsuApiScore;
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
		try {
			return getPP(acc, beatmap.MaxCombo(), 0);
		} catch (UserException e) {
			// this should have been allowed to get here.
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getPP(double acc, int combo, int misses) throws UserException {
		AccuracyDistribution dist = AccuracyDistribution.model(beatmap.getObjectCount(), misses, acc);

		return getPP(dist.getX100(), dist.getX50(), combo, dist.getMiss());
	}

	@Override
	public double getPP(int x100, int x50, int combo, int misses) {
		int x300 = beatmap.getObjectCount() - x50 - x100;

		OsuApiScore fakeScore = new OsuApiScore();
		fakeScore.setMaxCombo(combo);
		fakeScore.setCount300(x300);
		fakeScore.setCount100(x100);
		fakeScore.setCount50(x50);
		fakeScore.setCountMiss(misses);

		OsuPerformanceAttributes attributes =
				new OsuPerformanceCalculator().CreatePerformanceAttributes(fakeScore, beatmap);

		return attributes.total();
	}

	@Override
	public double getStarDiff() {
		return beatmap.StarDiff();
	}

	@Override
	public double getApproachRate() {
		return beatmap.ApproachRate();
	}

	@Override
	public double getOverallDifficulty() {
		return beatmap.OverallDifficulty();
	}
}
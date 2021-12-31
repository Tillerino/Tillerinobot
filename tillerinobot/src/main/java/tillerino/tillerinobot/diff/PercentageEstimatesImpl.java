package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Getter;

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
		AccuracyDistribution dist = AccuracyDistribution.get(beatmap.getObjectCount(), 0, acc);

		OsuScore score = new OsuScore((int) beatmap.DifficultyAttribute(getMods(), Beatmap.MaxCombo),
				dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(), getMods());

		return score.getPP(beatmap);
	}

	@Override
	public double getPP(double acc, int combo, int misses) {
		AccuracyDistribution dist = AccuracyDistribution.get(beatmap.getObjectCount(), misses, acc);

		OsuScore score = new OsuScore(combo, dist.getX300(), dist.getX100(), dist.getX50(), dist.getMiss(),
				getMods());

		return score.getPP(beatmap);
	}

	@Override
	public double getPP(int x100, int x50, int combo, int misses) {
		int x300 = beatmap.getObjectCount() - x50 - x100;
		OsuScore score = new OsuScore(combo, x300, x100, x50, misses, getMods());

		return score.getPP(beatmap);
	}

	@Override
	public double getStarDiff() {
		return beatmap.getStarDiff();
	}

	@Override
	public double getApproachRate() {
		return beatmap.getApproachRate();
	}

	@Override
	public double getOverallDifficulty() {
		return beatmap.getOverallDifficulty();
	}
}
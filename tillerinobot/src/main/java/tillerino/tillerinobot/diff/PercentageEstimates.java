package tillerino.tillerinobot.diff;

import javax.annotation.CheckForNull;

import org.tillerino.osuApiModel.types.BitwiseMods;

public interface PercentageEstimates {
	public default double getPPForAcc(double acc) {
		AccuracyDistribution dist = AccuracyDistribution.get(getAllObjectsCount(), 0, acc);
		
		CStandardScore score = new CStandardScore(
				(int) getBeatmap().DifficultyAttribute(getMods(), CBeatmap.MaxCombo),
				dist.get_300(),dist.get_100(), dist.get_50(), dist.getMiss(), getMods());
		
		return score.getPP(getBeatmap());
	}

	public default double getPP(double acc, int combo, int misses) {
		AccuracyDistribution dist = AccuracyDistribution.get(getAllObjectsCount(), misses, acc);

		CStandardScore score = new CStandardScore(combo, dist.get_300(), dist.get_100(), dist.get_50(), dist.getMiss(), getMods());

		return score.getPP(getBeatmap());
	}
	
	@BitwiseMods
	long getMods();
	
	boolean isShaky();

	@CheckForNull
	Double getStarDiff();
	
	CBeatmap getBeatmap();
	
	int getAllObjectsCount();
	
	boolean isOppaiOnly();
	
	boolean isRanked();
}
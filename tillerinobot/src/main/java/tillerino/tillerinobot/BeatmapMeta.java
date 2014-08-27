package tillerino.tillerinobot;

import org.tillerino.osuApiModel.OsuApiBeatmap;


public interface BeatmapMeta {
	public interface Estimates {
		
	}
	
	public interface OldEstimates extends Estimates {
		Integer getMaxPP();
		
		double getCommunityPP();
		
		boolean isTrustCommunity();

		boolean isTrustMax();
	}
	
	public interface PercentageEstimates extends Estimates {
		double getPPForAcc(double acc);
	}
	
	OsuApiBeatmap getBeatmap();

	Integer getPersonalPP();
	
	Estimates getEstimates();
	
	/**
	 * mods for this beatmap meta. this may not be equal to the mods of a recommendation.
	 * @return
	 */
	long getMods();
}

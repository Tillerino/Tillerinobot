package tillerino.tillerinobot;

import org.tillerino.osuApiModel.OsuApiBeatmap;


public interface BeatmapMeta {
	OsuApiBeatmap getBeatmap();
	
	double getCommunityPP();

	boolean isTrustCommunity();

	boolean isTrustMax();

	Integer getMaxPP();
	
	Integer getPersonalPP();
}

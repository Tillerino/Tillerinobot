package tillerino.tillerinobot.recommendations;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

/**
 * Recommendation as returned by the backend. Needs to be enriched before being displayed.
 * 
 * @author Tillerino
 */
public interface BareRecommendation {
	@BeatmapId
	int getBeatmapId();
	
	/**
	 * mods for this recommendation
	 * @return 0 for no mods, -1 for unknown mods, any other long for mods according to {@link Mods}
	 */
	@BitwiseMods
	long getMods();
	
	long[] getCauses();
	
	/**
	 * returns a guess at how much pp the player could achieve for this recommendation
	 * @return null if no personal pp were calculated
	 */
	Integer getPersonalPP();
	
	/**
	 * @return this is not normed, so the sum of all probabilities can be greater than 1 and this must be accounted for!
	 */
	double getProbability();
}
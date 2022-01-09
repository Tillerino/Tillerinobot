package tillerino.tillerinobot.recommendations;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Recommendation as returned by the backend. Needs to be enriched before being displayed.
 * 
 * @author Tillerino
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public record BareRecommendation(
		@BeatmapId int beatmapId,

		/**
		 * mods for this recommendation
		 * 
		 * @return 0 for no mods, -1 for unknown mods, any other long for mods according
		 *         to {@link Mods}
		 */
		@BitwiseMods long mods,

		long[] causes,

		/**
		 * returns a guess at how much pp the player could achieve for this
		 * recommendation
		 * 
		 * @return null if no personal pp were calculated
		 */
		Integer personalPP,

		/**
		 * @return this is not normed, so the sum of all probabilities can be greater
		 *         than 1 and this must be accounted for!
		 */
		double probability) {
}
package tillerino.tillerinobot.recommendations;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BeatmapMeta;

/**
 * Enriched Recommendation.
 * 
 * @author Tillerino
 */
@RequiredArgsConstructor
public class Recommendation {
	public final BeatmapMeta beatmap;
	
	public final BareRecommendation bareRecommendation;
}
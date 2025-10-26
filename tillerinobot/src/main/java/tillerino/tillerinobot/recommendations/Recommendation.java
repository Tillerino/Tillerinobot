package tillerino.tillerinobot.recommendations;

import tillerino.tillerinobot.BeatmapMeta;

/**
 * Enriched Recommendation.
 *
 * @author Tillerino
 */
public record Recommendation(BeatmapMeta beatmap, BareRecommendation bareRecommendation) {}

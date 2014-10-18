package tillerino.tillerinobot.mbeans;

public interface RecommendationsManagerMXBean {
	CacheMXBean fetchSamplers();

	CacheMXBean fetchGivenRecommendations();
}

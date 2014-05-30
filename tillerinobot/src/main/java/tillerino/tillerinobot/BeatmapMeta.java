package tillerino.tillerinobot;


public interface BeatmapMeta {
	String getArtist();

	String getTitle();

	String getVersion();

	int getBeatmapid();

	double getCommunityPP();

	boolean isTrustCommunity();

	boolean isTrustMax();

	Integer getMaxPP();

	double getStarDifficulty();
}

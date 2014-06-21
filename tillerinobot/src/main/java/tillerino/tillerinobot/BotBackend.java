package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;

public interface BotBackend {
	/**
	 * @param beatmapid
	 * @return null if not found
	 */
	public BeatmapMeta loadBeatmap(int beatmapid);

	public Recommendation loadRecommendation(String userNick, String message) throws SQLException, IOException, UserException;

	public Recommendation getLastRecommendation(String nick);

	public String getCause(String nick, int beatmapid) throws IOException;

	public void saveGivenRecommendation(String nick, int beatmapid) throws SQLException;

}
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

	/**
	 * @return the last version of the bot that was visited by this user. -1 if no information available.
	 */
	public int getLastVisitedVersion(String nick) throws SQLException;
	
	public void setLastVisitedVersion(String nick, int version) throws SQLException;
}
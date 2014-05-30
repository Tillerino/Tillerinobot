package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;

public interface BotBackend {

	public BeatmapMeta loadBeatmap(int beatmapid);

	public Recommendation loadRecommendation(String userNick, String message) throws SQLException, IOException, UserException;

	public Recommendation getLastRecommendation(String nick);

	public String getCause(String nick, int beatmapid) throws IOException;

}
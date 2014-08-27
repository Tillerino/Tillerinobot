package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.OsuApiUser;

public interface BotBackend {
	/**
	 * @param beatmapid
	 * @param mods TODO
	 * @return null if not found
	 * @throws IOException 
	 * @throws UserException 
	 */
	public BeatmapMeta loadBeatmap(int beatmapid, long mods) throws SQLException, IOException, UserException;

	public Recommendation loadRecommendation(@Nonnull String userNick, @Nonnull String message) throws SQLException, IOException, UserException;

	public Recommendation getLastRecommendation(@Nonnull String nick);

	public String getCause(@Nonnull String nick, int beatmapid) throws IOException;

	public void saveGivenRecommendation(@Nonnull String nick, int beatmapid) throws SQLException;

	/**
	 * @return the last version of the bot that was visited by this user. -1 if no information available.
	 */
	public int getLastVisitedVersion(@Nonnull String nick) throws SQLException, UserException;
	
	public void setLastVisitedVersion(@Nonnull String nick, int version) throws SQLException;

	/**
	 * resolve an IRC user name and get the user's information
	 * @param ircNick IRC nickname
	 * @param maxAge maximum age of the information. if <= 0 any cached information, if available, will be returned
	 * @return null if the user name can't be resolved
	 * @throws SQLException
	 * @throws IOException
	 */
	@CheckForNull
	public OsuApiUser getUser(@Nonnull String ircNick, long maxAge) throws SQLException, IOException;
	
	public void registerActivity(@Nonnull String nick);
	
	public long getLastActivity(@Nonnull OsuApiUser user) throws SQLException;

	public int getDonator(@Nonnull OsuApiUser user) throws SQLException, IOException;
}
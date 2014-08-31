package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;

public interface BotBackend {
	/**
	 * @param beatmapid
	 * @param mods mods for {@link PercentageEstimates}. These might be ignored if they can't be satisfied
	 * @return null if not found
	 * @throws IOException 
	 * @throws UserException 
	 */
	public BeatmapMeta loadBeatmap(int beatmapid, long mods) throws SQLException, IOException, UserException;

	public void saveGivenRecommendation(@Nonnull String nick, int beatmapid) throws SQLException;

	/**
	 * @return the last version of the bot that was visited by this user. -1 if no information available.
	 */
	public int getLastVisitedVersion(@Nonnull String nick) throws SQLException, UserException;
	
	Set<Integer> loadGivenRecommendations(@Nonnull String ircName) throws SQLException;

	public void setLastVisitedVersion(@Nonnull String nick, int version) throws SQLException;

	/**
	 * get a user's information
	 * @param userid user id
	 * @param maxAge maximum age of the information. if <= 0 any cached information, if available, will be returned
	 * @return null if the user can't be found
	 * @throws SQLException
	 * @throws IOException API exception
	 */
	@CheckForNull
	public OsuApiUser getUser(int userid, long maxAge) throws SQLException, IOException;
	
	public void registerActivity(int userid) throws SQLException;
	
	public long getLastActivity(@Nonnull OsuApiUser user) throws SQLException;

	public int getDonator(@Nonnull OsuApiUser user) throws SQLException, IOException;
	
	/**
	 * resolve an IRC username
	 * @param ircName
	 * @return null if the name could not be resolved
	 * @throws SQLException
	 * @throws IOException API exception
	 */
	@CheckForNull
	public Integer resolveIRCName(@Nonnull String ircName) throws SQLException, IOException;

	/**
	 * will load a sampler
	 * @param userid
	 * @param exclude these maps will be excluded (give top50 and previously given recommendations)
	 * @param model selected model
	 * @param nomod don't recommend mods
	 * @param requestMods request specific mods (these will be included, but this won't exclude other mods)
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UserException
	 */
	public Collection<BareRecommendation> loadRecommendations(int userid, @Nonnull Collection<Integer> exclude,
			@Nonnull Model model, boolean nomod, long requestMods) throws SQLException, IOException, UserException;
}
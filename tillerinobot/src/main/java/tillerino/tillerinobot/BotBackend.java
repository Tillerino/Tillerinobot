package tillerino.tillerinobot;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.IRCName;

import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Model;

public interface BotBackend {
	/**
	 * @param beatmapid
	 * @param mods
	 *            mods for {@link PercentageEstimates}. These might be ignored
	 *            if they can't be satisfied
	 * @return null if not found
	 * @throws SQLException
	 * @throws IOException
	 * @throws UserException
	 * @throws InterruptedException 
	 */
	@CheckForNull
	public BeatmapMeta loadBeatmap(@BeatmapId int beatmapid, @BitwiseMods long mods, Language lang) throws SQLException, IOException, UserException, InterruptedException;

	/**
	 * @param nick
	 * @return the last version of the bot that was visited by this user. -1 if
	 *         no information available.
	 * @throws SQLException
	 * @throws UserException
	 */
	public int getLastVisitedVersion(@Nonnull @IRCName String nick) throws SQLException, UserException;
	
	public void setLastVisitedVersion(@Nonnull @IRCName String nick, int version) throws SQLException;

	/**
	 * Get a user's information. If the user is not known in the database, the data will be downloaded from the osu API.
	 *
	 * @param userid user id
	 * @param maxAge Maximum age of the information in milliseconds.
	 *          If there is cached information in the database which is younger, the cached information will be returned.
	 *          Otherwise, fresh data will be downloaded and cached.
	 *          If <= 0, any cached information, if available, will be returned. 
	 * @return null if the user can't be found at the osu API
	 * @throws SQLException
	 * @throws IOException API exception
	 */
	@CheckForNull
	public OsuApiUser getUser(@UserId int userid, long maxAge) throws SQLException, IOException;

	/**
	 * Registers activity of a user. This information is not historized, i.e. a
	 * single value is updated and the previous value is therefore erased. This
	 * information is used to decide when to update top-scores of the users.
	 *
	 * The implementation is reasonably fast and can be called frequently.
	 * It does not download anything from the API and is unrelated to {@link #getUser(int, long)}.
	 *
	 * @param userid osu user ID of a real user. It is not required that the user is known via {@link #getUser(int, long)}.
	 * @throws SQLException only on connection errors
	 */
	public void registerActivity(@UserId int userid) throws SQLException;
	
	public long getLastActivity(@Nonnull OsuApiUser user) throws SQLException;

	/**
	 * Checks if a user is a donator/patron.
	 *
	 * @return a positive value if the user is a donator/patron.
	 */
	public int getDonator(@Nonnull OsuApiUser user) throws SQLException, IOException;
	
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
	public Collection<BareRecommendation> loadRecommendations(@UserId int userid, @Nonnull Collection<Integer> exclude,
			@Nonnull Model model, boolean nomod, @BitwiseMods long requestMods) throws SQLException, IOException, UserException;
	
	/**
	 * Retreives beatmap. Implementation hint: this might be called a *lot* when
	 * checking recommendation predicates and should probably be cached.
	 * 
	 * @param beatmapId
	 * @return null if not found
	 * @throws IOException
	 * @throws SQLException
	 */
	public @CheckForNull OsuApiBeatmap getBeatmap(@BeatmapId int beatmapId) throws SQLException, IOException;
	
	/**
	 * links the given user to a Patreon account using a token string.
	 * 
	 * @param token a token that was emailed to the Patron.
	 * @param user osu account to link to
	 * @return the name of the Patreon account that current user was linked to, or null if the token was not valid
	 */
	@CheckForNull
	public String tryLinkToPatreon(String token, OsuApiUser user);

	/**
	 * Retrieves the last plays from this user. These don't have pp and might be failed attempts.
	 * @param userid
	 * @return sorted from most recent to oldest
	 * @throws IOException
	 */
	@Nonnull public List<OsuApiScore> getRecentPlays(@UserId int userid) throws IOException;

	@CheckForNull
	public OsuApiUser downloadUser(String userName) throws IOException, SQLException;
}

package tillerino.tillerinobot.lang;

import java.util.List;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * Implementations of this interface will be instantiated once per user to allow
 * for shuffling of messages, counters and time delay measurements. This is why
 * implementations need to be kept very lightweight.
 * 
 * @author Tillerino
 */
public interface Language {
	/**
	 * The requested beatmap is not known. The reason for this is usually that
	 * it's too new or too easy (however weird that may sound).
	 * 
	 * @return
	 */
	String unknownBeatmap();

	/**
	 * An exception occurred and has been logged with a marker. The message here
	 * should indicated that if the error occurs repeatedly, Tillerino should be
	 * contacted via Twitter @Tillerinobot or reddit /u/Tillerino
	 * 
	 * @param marker
	 *            the marker to reference the log entry. short string - six or
	 *            eight characters
	 * @return
	 */
	String exception(String marker);

	/**
	 * No information was available for the given mods. This message will be
	 * appended to the song info in brackets.
	 * 
	 * @return
	 */
	String noInformationForModsShort();

	/**
	 * No information was available for the given mods. This message will be
	 * displayed by itself, so it can be longer.
	 * 
	 * @return
	 */
	String noInformationForMods();

	/**
	 * Welcome a donator who has been offline. This can include multiple
	 * messages and even actions.
	 * 
	 * @param user
	 *            communication interface
	 * @param apiUser
	 *            for more information about the user
	 * @param inactiveTime
	 *            time since the user was last seen in #osu in milliseconds
	 */
	void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime);

	/**
	 * The entire command that the user typed is not known.
	 * 
	 * @param command
	 *            does not include the leading exclamation mark.
	 * @return
	 */
	String unknownCommand(String command);

	/**
	 * given mods in !with command could not be interpreted
	 * 
	 * @param mods
	 *            the given mods
	 * @return
	 */
	String malformattedMods(String mods);

	/**
	 * !with was used, but the bot can't remember the last song info that was
	 * given. This may be because it was restarted or because the cache timed
	 * out.
	 * 
	 * @return
	 */
	String noLastSongInfo();

	/**
	 * Short string to suggest to try this recommendation with mods if the song
	 * info doesn't include that information already. Appended to song info.
	 * 
	 * @return
	 */
	String tryWithMods();

	/**
	 * Short string to suggest to try this recommendation with the given mods if
	 * the song info doesn't include that information already. Appended to song
	 * info.
	 * 
	 * @param mods
	 *            the suggested mods
	 * @return
	 */
	String tryWithMods(List<Mods> mods);

	/**
	 * The user's IRC nick name could not be resolved to an osu user id. The
	 * message should suggest to contact @Tillerinobot or /u/Tillerino.
	 * 
	 * @param exceptionMarker
	 *            a marker to reference the created log entry. six or eight
	 *            characters.
	 * @param ircNick
	 *            the irc nick which could not be resolved
	 * @return
	 */
	String unresolvableName(String exceptionMarker, String ircNick);

	/**
	 * A rare internal error has occurred, which is no cause for concern. Rather
	 * than admiting that an error occurred, this message should make an excuse
	 * why the request could not be fulfilled.
	 * 
	 * @return
	 */
	String excuseForError();

	/**
	 * Response to the !complain command.
	 * 
	 * @return
	 */
	String complaint();

	/**
	 * Donator mentioned hug or hugs. Response can include multiple messages or
	 * even actions.
	 * 
	 * @param user
	 *            communication interface.
	 * @param apiUser
	 *            user object for more info
	 */
	void hug(IRCBotUser user, OsuApiUser apiUser);

	/**
	 * Response to !help command.
	 * 
	 * @return
	 */
	String help();

	/**
	 * Response to !faq command.
	 * 
	 * @return
	 */
	String faq();

	/**
	 * A part of the !recommend command was not recognized.
	 * 
	 * @param param
	 *            the part of the command which was not recognized.
	 * @return
	 */
	String unknownRecommendationParameter(String param);

	/**
	 * A feature is rank restricted.
	 * 
	 * @param feature
	 *            The feature's name.
	 * @param minRank
	 *            The minimum rank to be able to use this feature.
	 * @param user
	 *            ther user who is requesting the feature
	 * @return
	 */
	String featureRankRestricted(String feature, int minRank, OsuApiUser user);

	/**
	 * The user requested a recommendation and both gave a mod and the nomod
	 * option.
	 * 
	 * @return
	 */
	String mixedNomodAndMods();

	/**
	 * The current recommendations sampler is empty. "try again to start over".
	 * 
	 * @return
	 */
	String outOfRecommendations();

	/**
	 * The requested beatmap is not ranked.
	 * 
	 * @return
	 */
	String notRanked();

	/**
	 * Comment after beatmap info was sent in response to /np
	 */
	void optionalCommentOnNP(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta);

	/**
	 * Comment after beatmap info was sent in response to !with
	 */
	void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta);
	
	/**
	 * Comment after beatmap info was sent in response to !recommend
	 */
	void optionalCommentOnRecommendation(IRCBotUser user, OsuApiUser apiUser, Recommendation recommendation);
}
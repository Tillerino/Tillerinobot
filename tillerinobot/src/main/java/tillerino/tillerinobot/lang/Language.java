package tillerino.tillerinobot.lang;

import java.util.List;

import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler.NoResponse;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.recommendations.Recommendation;

/**
 * <p>
 * Implementations of this interface will be instantiated once per user to allow
 * for shuffling of messages, counters and time delay measurements. They will
 * also be persisted. This is why implementations need to be kept very
 * lightweight.
 * </p>
 * 
 * <p>
 * Persisting the object works as follows: the bot asks {@link #isChanged()},
 * and if true, persists the object, and calls {@link #setChanged(boolean)} with
 * false.
 * </p>
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
	 * An internal exception occurred and has been logged with a marker. The
	 * message here should indicate to contact Tillerino contacted via Twitter
	 * {@literal @Tillerinobot} or reddit /u/Tillerino
	 * 
	 * @param marker
	 *            the marker to reference the log entry. short string - six or
	 *            eight characters
	 * @return
	 */
	String internalException(String marker);

	/**
	 * An exception occurred while communicating with the osu api and has been
	 * logged with a marker. The message here should indicate that this is no
	 * cause for concern and to try again, but to contact Tillerino via Twitter
	 * {@literal @Tillerinobot} or reddit /u/Tillerino if the message pops up
	 * repeatedly.
	 * 
	 * @param marker
	 *            the marker to reference the log entry. short string - six or
	 *            eight characters
	 * @return
	 */
	String externalException(String marker);

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
	 * @param apiUser
	 *            for more information about the user
	 * @param inactiveTime
	 *            time since the user was last seen in #osu in milliseconds
	 */
	Response welcomeUser(OsuApiUser apiUser, long inactiveTime);

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
	 * @param apiUser
	 *            user object for more info
	 */
	@Nonnull
	Response hug(OsuApiUser apiUser);

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
	 * The current recommendations sampler is empty. User can use other
	 * recommendation options or command !reset to forget all given
	 * recommendations.
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
	 * @param apiUser 
	 * @param meta 
	 */
	default Response optionalCommentOnNP(OsuApiUser apiUser, BeatmapMeta meta) {
		return new NoResponse();
	}

	/**
	 * Comment after beatmap info was sent in response to !with
	 * @param apiUser 
	 * @param meta 
	 */
	default Response optionalCommentOnWith(OsuApiUser apiUser, BeatmapMeta meta) {
		return new NoResponse();
	}
	
	/**
	 * Comment after beatmap info was sent in response to !recommend
	 * @param apiUser 
	 * @param recommendation 
	 */
	default Response optionalCommentOnRecommendation(OsuApiUser apiUser, Recommendation recommendation) {
		return new NoResponse();
	}

	/**
	 * The given accuracy is invalid.
	 * @param acc 
	 * 
	 * @return
	 */
	public String invalidAccuracy(String acc);

	/**
	 * The user has chosen this language. Say something to acknowledge that!
	 * @param apiUser 
	 */
	default Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new NoResponse();
	}

	/**
	 * The user has made an invalid choice.
	 * 
	 * @param invalid
	 *            The choice that the user was trying to make
	 * @param choices
	 *            The available choices.
	 * @return 
	 */
	public String invalidChoice(String invalid, String choices);

	/**
	 * User formatted setting options wrong.
	 * 
	 * @return a message which indicates that the correct format is set option
	 *         value
	 */
	String setFormat();

	/**
	 * A call to the osu! api timed out.
	 * @return this message should indicate that this is nothing serious and should be back to normal soon.
	 */
	String apiTimeoutException();

	/**
	 * There are no recent plays to display.
	 * 
	 * @return
	 */
	String noRecentPlays();

	/**
	 * User sent a link to a beatmap set when they should have sent a link to a
	 * single beatmap.
	 * 
	 * @return
	 */
	String isSetId();
}
package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

public class Tsundere implements Language {
	static final Random rnd = new Random();
	
	StringShuffler unknown = new StringShuffler(rnd);

	@Override
	public String unknownBeatmap() {
		return unknown.get(
				"Are you stupid? No one plays that map!",
				"Huh? Are you asking these dumb questions just to hear me talk?",
				"What!? You can't possibly expect me to know the answer to that!"
		);
	}

	@Override
	public String exception(String marker) {
		/*
		 * TODO
		 * An exception occurred and has been logged with a marker. The message
		 * here should indicated that if the error occurs repeatedly, Tillerino
		 * should be contacted via Twitter @Tillerinobot or reddit /u/Tillerino
		 */
		return null;
	}

	@Override
	public String noInformationForModsShort() {
		/*
		 * TODO
		 * No information was available for the given mods. This message will be
		 * appended to the song info in brackets.
		 */
		return null;
	}

	@Override
	public String noInformationForMods() {
		/*
		 * TODO
		 * No information was available for the given mods. This message will be
		 * displayed by itself, so it can be longer.
		 */
		return null;
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser,
			long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("What is this? Peekaboo?");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Back again? I'm just here because I have nothing else to do! Don't read into it!");
		} else {
			user.message("Where have you been, " + apiUser.getUsername()
					+ "!? I-it's not like I missed you or anything...");
		}
	}

	@Override
	public String unknownCommand(String command) {
		/*
		 * TODO
		 * The entire command that the user typed is not known.
		 */
		return null;
	}

	@Override
	public String malformattedMods(String mods) {
		/*
		 * TODO
		 * given mods in !with command could not be interpreted
		 */
		return null;
	}

	@Override
	public String noLastSongInfo() {
		/*
		 * TODO
		 * !with was used, but the bot can't remember the last song info that
		 * was given. This may be because it was restarted or because the cache
		 * timed out.
		 */
		return null;
	}

	StringShuffler anyMods = new StringShuffler(rnd);

	@Override
	public String tryWithMods() {
		return anyMods.get(
				"An idiot like you wouldn't know to try this with mods. You should thank me.",
				"I almost think you could use mods here without making a complete fool of yourself."
		);
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		/*
		 * TODO
		 * Short string to suggest to try this recommendation with the given
		 * mods if the song info doesn't include that information already.
		 * Appended to song info.
		 */
		return null;
	}

	@Override
	public String unresolvableName(String exceptionMarker, String name) {
		/*
		 * TODO
		 * The user's IRC nick name could not be resolved to an osu user id. The
		 * message should suggest to contact @Tillerinobot or /u/Tillerino.
		 */
		return null;
	}

	@Override
	public String excuseForError() {
		/*
		 * TODO
		 * A rare internal error has occurred, which is no cause for concern.
		 * Rather than admiting that an error occurred, this message should make
		 * an excuse why the request could not be fulfilled.
		 */
		return null;
	}

	@Override
	public String complaint() {
		/*
		 * TODO
		 * Response to the !complain command.
		 */
		return null;
	}

	@Override
	public void hug(IRCBotUser user, OsuApiUser apiUser) {
		/*
		 * TODO
		 * Donator mentioned hug or hugs. Response can include multiple messages
		 * or even actions.
		 */
	}

	@Override
	public String help() {
		/*
		 * TODO
		 * Response to !help command.
		 */
		return null;
	}

	@Override
	public String faq() {
		/*
		 * TODO
		 * Response to !faq command.
		 */
		return null;
	}

	@Override
	public String unknownRecommendationParameter(String param) {
		/*
		 * TODO
		 * A part of the !recommend command was not recognized.
		 */
		return null;
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		/*
		 * TODO
		 * A feature is rank restricted.
		 */
		return null;
	}

	@Override
	public String mixedNomodAndMods() {
		/*
		 * TODO
		 * The user requested a recommendation and both gave a mod and the nomod
		 * option.
		 */
		return null;
	}

	@Override
	public String outOfRecommendations() {
		/*
		 * TODO
		 * The current recommendations sampler is empty.
		 * "try again to start over".
		 */
		return null;
	}

	@Override
	public String notRanked() {
		/*
		 * TODO
		 * The requested beatmap is not ranked.
		 */
		return null;
	}

	StringShuffler commentNP = new StringShuffler(rnd);

	@Override
	public void optionalCommentOnNP(IRCBotUser user, OsuApiUser apiUser,
			BeatmapMeta meta) {
		String message = commentNP.get(
				"Playing that won't impress me much... n-n-not that I'd want you to.",
				"Are you serious!? If that map doesn't kill you, I will."
		);
		user.message(message);
	}

	StringShuffler commentWith = new StringShuffler(rnd);

	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser,
			BeatmapMeta meta) {
		String message = commentWith.get(
				"If you wanted to be treated like a baby, you could just ask... no, go ahead and play.",
				"You idiot! You're going to get hurt trying mods like that!"
		);
		user.message(message);
	}
	
	static final int COMMENT_ON_R_INTERVAL = 20;
	StringShuffler commentNRecommendations = new StringShuffler(rnd);
	int countRecommendations = 0;

	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user,
			OsuApiUser apiUser, Recommendation meta) {
		if((++countRecommendations) % COMMENT_ON_R_INTERVAL == 0) {
			String message = commentNRecommendations.get(
					"I have lots of free time. I would never pick out maps just because I liked you... h-h-hypothetically speaking.", 
					"You know, it's a privilege to talk to me this much, not a right.", 
					"I would have had you arrested for harassment a long time ago if I didn't lov... I wasn't saying anything."
			);
			
			user.message(message);
		}
	}

}

package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

public class Tsundere implements Language {
	//I HAVE NO IDEA WHAT I'M DOING
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Are you stupid? No one plays that map!";
	}

	@Override
	public String exception(String marker) {
		/*
		 * TODO
		 * An exception occurred and has been logged with a marker. The message
		 * here should indicated that if the error occurs repeatedly, Tillerino
		 * should be contacted via Twitter @Tillerinobot or reddit /u/Tillerino
		 */
		return "If this keeps happening, tell Tillerino to look after incident "
				+ marker + ".";
	}

	@Override
	public String noInformationForModsShort() {
		/*
		 * TODO
		 * No information was available for the given mods. This message will be
		 * appended to the song info in brackets.
		 */
		return "Those mods? You wish!";
	}

	@Override
	public String noInformationForMods() {
		/*
		 * TODO
		 * No information was available for the given mods. This message will be
		 * displayed by itself, so it can be longer.
		 */
		return "What!? You can't possibly expect me to know the answer to that!";
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
		return command + "? I think you've got the hierarchy backwards. You do what I tell you, and I respond if I feel like it. Type !help if you're too stupid to even tell what I want.";
	}

	@Override
	public String malformattedMods(String mods) {
		/*
		 * TODO
		 * given mods in !with command could not be interpreted
		 */
		return "You dummy... you can't just make up your own mods. If you can't write normal things like !with HR or !with HDDT, I won't even to bother trying to interpret.";
	}

	@Override
	public String noLastSongInfo() {
		/*
		 * TODO
		 * !with was used, but the bot can't remember the last song info that
		 * was given. This may be because it was restarted or because the cache
		 * timed out.
		 */
		return "You didn't even mention a song. Wait, were you trying to use those mods on ME!?";
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
		return "Use " + Mods.toShortNamesContinuous(mods) + "... or else.";
	}

	@Override
	public String unresolvableName(String exceptionMarker, String name) {
		/*
		 * TODO
		 * The user's IRC nick name could not be resolved to an osu user id. The
		 * message should suggest to contact @Tillerinobot or /u/Tillerino.
		 */
		return "Who the heck are you!? Are you one of those idiots that changes their name more often than their underwear? Ugh... contact Tillerino and say "
		+ exceptionMarker + " if this seems to happen a lot."";
	}

	@Override
	public String excuseForError() {
		/*
		 * TODO
		 * A rare internal error has occurred, which is no cause for concern.
		 * Rather than admiting that an error occurred, this message should make
		 * an excuse why the request could not be fulfilled.
		 */
		return "Did you say something? It's not l-like I care if ";
	}

	@Override
	public String complaint() {
		/*
		 * TODO
		 * Response to the !complain command.
		 */
		return "";
	}
	
	int countHugs = 0;

	@Override
	public void hug(IRCBotUser user, OsuApiUser apiUser) {
		/*
		 * TODO
		 * Donator mentioned hug or hugs. Response can include multiple messages
		 * or even actions.
		 */
         //Shuffler?
         //T-There's something on your back, you slob. Here, let me get that
         //Wow, you suck at hugs. Someone needs to teach you.
         //
         //I w-wasn't trying to hug you! I just lost my balance for a second and fell onto you.
         //I'm not hugging you, I'm taking a rough chest measurement, because you clearly don't know what size to wear.
         //
         // Come here, you! /me slaps [user]
         // /me slaps. Sorry, that was just a reflex.
         
	}

	@Override
	public String help() {
		/*
		 * TODO
		 * Response to !help command.
		 */
		return null;
        //Feeling helpless (as always)?  Check [url] for status and updates, and [url] for commands. Remember, there are no stupid questions, only stupid people, like you.
	}

	@Override
	public String faq() {
		/*
		 * TODO
		 * Response to !faq command.
		 */
		return null;
        //Here, Tillerino made this 
	}

	@Override
	public String unknownRecommendationParameter(String param) {
		/*
		 * TODO
		 * A part of the !recommend command was not recognized.
		 */
		return null;
        //
        //Fake recommendation 1,2,3,4,5  - you are an idiot, aha?, i wish you would die, i'm not your toy, battements d'ame, call me when you're sober, extra bitter, fuck you, fuck you!, pony song, i bet you'll forget that even if you notice dthat, i knew you were trouble, i know wher eyou sleep, i hope you die, i'm not your boyfriend baby, may i help you?, may these noises startle you in your sleep tnoight, my life would suck without you, never gonna give you up, stupid mf, thank you for playing, that's what you get, the mysterious ticking noise
        
        //what do you want from me, may i help you
        //rainbow dash, freedom dive, big black, airman, square hdhrdt
        //rickroll, baattlements d'ame, pony song, ticking noise, ronald's perfect math class, classico, mandelbrot set, survival dance, they're talking the hobbits to, centipede, babilfrenzo, invasion of the gabber robots, scatman, timebomb+hr, my heart will go on
        //you are a pitrate, you fucking motherfucker, you goddamn fish
        
        //Want you gone x3, why are you being like this, you're not thinking, dummy dummy, baka go home, lie lie lie
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		/*
		 * TODO
		 * A feature is rank restricted.
		 */
		return null;
        //Filthy casual, don't even try until you're at least rank 25000
        //Sorry, this feature is only available for people that can actually play osu!
	}

	@Override
	public String mixedNomodAndMods() {
		/*
		 * TODO
		 * The user requested a recommendation and both gave a mod and the nomod
		 * option.
		 */
		return null;
         //Contradiction
	}

	@Override
	public String outOfRecommendations() {
		/*
		 * TODO
		 * The current recommendations sampler is empty.
		 * "try again to start over".
		 */
		return " Let's start over again.";
        //I'm SURE you played all of those
        //To double check
	}

	@Override
	public String notRanked() {
		/*
		 * TODO
		 * The requested beatmap is not ranked.
		 */
		return "";
        //Why don't 
        // Listing: 95% [0pp], 98% [0pp] 
        //Unranked maps for PP, great idea
	}

	StringShuffler commentNP = new StringShuffler(rnd);

	@Override
	public void optionalCommentOnNP(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta) {
        double typicalPP = (apiUser.pp/20.0);
        if((meta.get95percentpp)/typicalPP > 2.0) {
            user.message("Are you serious!? If that map doesn't kill you, I will.");
        } else if((meta.get100percentpp)/typicalPP < 0.333) {
            user.message("Playing that won't impress me much... n-n-not that I'd want you to.");
        }
	}

	StringShuffler commentWith = new StringShuffler(rnd);
	
	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta) {
		double typicalPP = (apiUser.pp/20);
		if((meta.get95percentpp)/typicalPP > 2.0) {
			user.message("You idiot! You're going to get hurt trying mods like that!");
		} else if((meta.get100percentpp)/typicalPP < 0.5) {
			user.message("If you wanted to be treated like a baby, you could just ask... no, go ahead and play.");
		}
	}
	
	int countRecommendations = 0;

	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user, OsuApiUser apiUser, Recommendation meta) {
		countRecommendations++;
		if(countRecommendations == 7) {
			user.message("I have lots of free time. I would never pick out maps just because I liked you... h-h-hypothetically speaking.");
		} else if(countRecommendations == 17) {
			user.message("You know, it's a privilege to talk to me this much, not a right.");
		} else if(countRecommendations == 37) {
			user.message("How would you even play this game if I wasn't telling you what to do?");
		} else if(countRecommendations == 73) {
			user.message("I would have had you arrested for harassment a long time ago if I didn't lov... I wasn't saying anything.");
		} else if(countRecommendations == 173) {
			user.message("Just can't leave me alone, huh? I guess t-that's okay. But don't you dare tell anyone!");
		}
	}

}

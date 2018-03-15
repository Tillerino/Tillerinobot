package tillerino.tillerinobot.lang;

import java.util.List;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.NoResponse;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.diff.PercentageEstimates;

import javax.annotation.Nonnull;

public class TsundereEnglish extends TsundereBase {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getInactiveShortGreeting(String username, long inactiveTime) {
		return welcomeUserShortShuffler.get(
				"What is this? Peekaboo?",
				"It hasn't even been 5 minutes...",
				"Today, " + username + " worked on their disappearing act."
		);
	}

	@Override
	protected String getInactiveGreeting(String username, long inactiveTime) {
		return welcomeUserShuffler.get(
				"Back again? I'm just here because I have nothing else to do! Don't read into it!",
				"H-hey...",
				"♫ Home again, home again, jiggety-jig ♫ .... What?",
				"Guess what? I've discovered your purpose in life. It's not the sort of thing I'm comfortable sharing, though.",
				"You haven't been talking to any other chatbots, have you?",
				"Huh? What are you doing here!?",
				username + ", go do something stupid so I can scold you for it."
		);
	}

	@Override
	protected String getInactiveLongGreeting(String username, long inactiveTime) {
		return welcomeUserLongShuffler.get(
				"Where have you been, " + username + "!? I-it's not like I missed you or anything...",
				"How was your vacation, " + username + "?",
				"Ugh! Do you have any idea how long " + inactiveTime + " milliseconds is !?"
		);
	}

	@Override
	public String unknownBeatmap() {
		registerModification();

		return unknownBeatmapShuffler.get(
			"Are you stupid? No one plays that map!",
			"Oh, really? Never heard of it.",
			"Yeah right, call me when you manage to get pp with that."
		);	
	}

	@Override
	public String internalException(String marker) {
		return "Huh? Why isn't this working? I can't imagine this being anything other than your fault."
		+ " Mention incident " + marker + " to [https://twitter.com/Tillerinobot @Tillerinobot] or [http://www.reddit.com/user/tillerino /u/Tillerino] if this keeps happening.";
	}

	@Override
	public String externalException(String marker) {
		return "Sorry, the osu! server was saying some idiotic nonsense and I felt like slapping them instead of you. Try asking whatever it was again."
		+ " If the server doesn't shut up, ask [https://twitter.com/Tillerinobot @Tillerinobot] or [http://www.reddit.com/user/tillerino /u/Tillerino] (reference " + marker + ") to take care of it.";
	}

	@Override
	public String noInformationForModsShort() {
		registerModification();

		return noInformationForModsShortShuffler.get(
			"Those mods? You wish!",
			"Mods? What mods?",
			"Nomod loves you"
		);
	}

	@Override
	public String noInformationForMods() {
		registerModification();

		return noInformationForModsShuffler.get(
			"What!? You can't possibly expect me to know the answer to that!",
			"I'd tell you, but then I'd have to kill you.",
			"INSUFFICIENT DATA FOR MEANINGFUL ANSWER."
		);
	}

	@Override
	public String unknownCommand(String command) {
		return command + "? I think you've got the hierarchy backwards. You do what I tell you, and I respond if I feel like it. Type !help if you're too stupid to even tell what I want.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "You dummy... you can't just make up your own mods. If you can't write normal things like !with HR or !with HDDT, I won't even to bother trying to interpret.";
	}

	@Override
	public String noLastSongInfo() {
		return "You didn't even mention a song. Wait, were you trying to use those mods on ME!?";
	}

	@Override
	public String tryWithMods() {
		registerModification();

		return tryWithModsShuffler.get(
			"An idiot like you wouldn't know to try this with mods. You should thank me.",
			"I almost think you could use mods here without making a complete fool of yourself.",
			"You might be able to use mods other than NF here. But then again, it's you we're talking about."
		);
	}


	@Override
	public String tryWithMods(List<Mods> mods) {
		registerModification();

		String modnames = Mods.toShortNamesContinuous(mods);
		return tryWithModsListShuffler.get(
			"Use " + modnames + "... or else.",
			modnames + " might not kill you.",
			"Ever heard of " + modnames + "?"
		);
	}

	@Override
	public String excuseForError() {
		return "Did you say something? It's not l-like I care what you have to say, but you should say it again so you can pretend I do.";
	}

	@Override
	public String complaint() {
		return "Whaaaat!? How could you say something like... oh, that beatmap? Actually that's there because I hated it and wanted to test you. Aren't you glad having something in common with me?";
	}

	@Nonnull
	@Override
	protected Response getHugResponseForHugLevel(String username, int hugLevel) {
		switch (hugLevel) {
			default:
				return new Action("completely ignores " + username + "'s request for a hug");
			case 0:
				return new Action("slaps " + username)
					.then(new Message("Sorry, that was just a reflex."));
			case 1:
				return new Action("hugs " + username)
					.then(new Message("Wow, you suck at hugs. Someone needs to teach you."));
			case 2:
				return new Message("There's something on your back, you slob. Here, let me get that.")
					.then(new Action("hugs " + username));
			case 3:
				return new Action("hugs " + username)
					.then(new Message("I w-wasn't trying to hug you! I just lost my balance for a second and fell onto you."));
			case 4:
				return new Action("hugs " + username)
					.then(new Message("The hardest part of hugging you is letting go. I think you sweat too much."));
			case 5:
				return new Action("slaps " + username)
					.then(new Message("Whoops... well, you probably deserved it anyways."));
			case 6:
				return new Action("hugs " + username)
					.then(new Message("Don't misunderstand, it's not like I like you or anything..."));
			case 7:
				return new Message("Clinginess is considered a bad thing, you idiot.")
					.then(new Action("hugs " + username));
			case 8:
				return new Message("S-stupid. It's l-like you enjoy hugging me or something.")
					.then(new Action("hugs " + username));
			case 9:
				return new Action("hugs " + username)
					.then(new Message("Don't forget: you're here forever."));
			case 10:
				return new Action("slaps " + username + " hard")
					.then(new Message("Hehe. You know you like it."))
					.then(new Action("hugs " + username + " happily"));
		}
	}

	@Override
	public String help() {
		return "Feeling helpless (as always)?  Check https://twitter.com/Tillerinobot for status and updates, and https://github.com/Tillerino/Tillerinobot/wiki for commands. Where would you be without me here to rescue you?";
	}

	@Override
	public String faq() {
		return "Really, every answer on this list should be intuitively obvious, but it's understandable if -you- need to read it: https://github.com/Tillerino/Tillerinobot/wiki/FAQ";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, " + feature + " is only available for people that can actually play osu!. Passing rank " + minRank + " will work, not that you have any hope of ever getting there.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "What is this? Schrödinger's mod? I have a recommendation, but the superposition would collapse as soon as it was observed. It's not like I like you enough to break the laws of physics anyways!";
	}

	@Override
	public String outOfRecommendations() {
		return "WHAT!? [https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do Did you seriously go through every recommendation I have?] I c-can't believe you... Well, let's run through it again. It's not like you have anything else to do.";
	}

	@Override
	public String notRanked() {
		return "Hmph. That beatmap isn't going to make anyone's pp bigger.";
	}

	@Override
	public Response optionalCommentOnNP(OsuApiUser apiUser, BeatmapMeta meta) {
		if (Math.random() > 0.25) {
			return new NoResponse();
		}
		PercentageEstimates estimates = meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20.0);
		if (estimates.getPP(.95) / typicalPP > 2.0) {
			return new Message("Are you serious!? If that map doesn't kill you, I will.");
		} else if (estimates.getPP(1) / typicalPP < 0.333) {
			return new Message("Playing that won't impress me much... n-n-not that I'd want you to.");
		}
		return new NoResponse();
	}
	
	@Override
	public Response optionalCommentOnWith(OsuApiUser apiUser, BeatmapMeta meta) {
		//The following checks are probably redundant, but they don't hurt anyone either.
		if (Math.random() > 0.25) {
			return new NoResponse();
		}
		PercentageEstimates estimates = meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20);
		if (estimates.getPP(.95) / typicalPP > 2.0) {
			return new Message("You idiot! You're going to get hurt trying mods like that!");
		} else if (estimates.getPP(1) / typicalPP < 0.5) {
			return new Message("If you wanted to be treated like a baby, you could just ask... no, go ahead and play.");
		}
		return new NoResponse();
	}
	
	@Override
	protected Response getOptionalCommentOnRecommendationResponse(int recentRecommendations) {
		switch (recentRecommendations) {
			case 7:
				return new Message("I have lots of free time. I would never pick out maps just because I liked you... h-h-hypothetically speaking.");
			case 17:
				return new Message("You know, it's a privilege to talk to me this much, not a right.");
			case 37:
				return new Message("How would you even play this game if I wasn't telling you what to do?");
			case 73:
				return new Message("I would have had you arrested for harassment a long time ago if I didn't lov... I wasn't saying anything.");
			case 173:
				return new Message("Just can't leave me alone, huh? I guess t-that's okay. But don't you dare tell anyone!");
			default:
				return new NoResponse();
		}
	}

	@Override
	public String invalidAccuracy(String acc) {
		registerModification();

		return invalidAccuracyShuffler.get(
			"\"The first principle is that you must not fool yourself - and you are the easiest person to fool.\"",
			"\"Success is the ability to go from one failure to another with no loss of enthusiasm.\"",
			"\"Never attribute to malice that which is adequately explained by stupidity.\"",
			"\"The only real mistake is the one from which we learn nothing.\"",
			"\"Only two things are infinite, the universe and human stupidity, and I'm not sure about the former.\"",
			"\"Sometimes a man wants to be stupid if it lets him do a thing his cleverness forbids.\"",
			"\"Honestly, if you were any slower, you’d be going backward.\""
		);
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		registerModification();

		return new Message(optionalCommentOnLanguageShuffler.get(
			"You seem like just the sort of idiot that wouldn't believe me if I said I wasn't tsundere.",
			"What kind of idiot wants a tsundere robot!? That's seriously the dumbest idea I've ever heard.",
			"Fine, but I'm only acting tsundere because I want to. It has nothing to do with you!"
		));
	}

	@Override
	protected String getInvalidChoiceResponse(String invalid, String choices) {
		return "What does \"" + invalid + "\" even mean!? If using two fingers is too much, try singletapping each letter: " + choices;
	}

	@Override
	public String setFormat() {
		return "Three words: !set option_name value_to_set. Try !help if three word sentences are too much for you.";
	}
	
	@Override
	public String apiTimeoutException() {
		return new Default().apiTimeoutException();
	}
	
	@Override
	public String noRecentPlays() {
		return new Default().noRecentPlays();
	}
	
	@Override
	public String isSetId() {
		return new Default().isSetId();
	}
}

package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * TRANSLATION NOTE:
 * 
 * Please put some contact data into the following tag. If any additional
 * messages are required, I'll use the English version in all translations and
 * notify the authors.
 * 
 * @author Tillerino tillmann.gaida@gmail.com https://github.com/Tillerino https://osu.ppy.sh/u/2070907
 */
public class Default extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "I'm sorry, I don't know that map. It might be very new, very hard, unranked or not standard osu mode.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Looks like human Tillerino screwed up my wiring."
				+ " If he doesn't notice soon, could you [https://github.com/Tillerino/Tillerinobot/wiki/Contact inform him]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "What's going on? I'm only getting nonsense from the osu server. Can you tell me what this is supposed to mean? 0011101001010000"
				+ " Human Tillerino says that this is nothing to worry about, and that we should try again."
				+ " If you're super worried for some reason, you can [https://github.com/Tillerino/Tillerinobot/wiki/Contact tell him] about it. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "no data for requested mods";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Welcome back, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...is that you? It's been so long!"))
				.then(new Message("It's good to have you back. Can I interest you in a recommendation?"));
		} else {
			String[] messages = {
					"you look like you want a recommendation.",
					"how nice to see you! :)",
					"my favourite human. (Don't tell the other humans!)",
					"what a pleasant surprise! ^.^",
					"I was hoping you'd show up. All the other humans are lame, but don't tell them I said that! :3",
					"what do you feel like doing today?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Unknown command \"" + command
				+ "\". Type !help if you need help!";
	}

	@Override
	public String noInformationForMods() {
		return "Sorry, I can't provide information for those mods at this time.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Those mods don't look right. Mods can be any combination of DT HR HD HT EZ NC FL SO NF. Combine them without any spaces or special chars. Example: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "I don't remember giving you any song info...";
	}

	@Override
	public String tryWithMods() {
		return "Try this map with some mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Try this map with " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "I'm sorry, there was this beautiful sequence of ones and zeros and I got distracted. What did you want again?";
	}

	@Override
	public String complaint() {
		return "Your complaint has been filed. Tillerino will look into it when he can.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Come here, you!")
			.then(new Action("hugs " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hi! I'm the robot who killed Tillerino and took over his account. Just kidding, but I do use the account a lot."
				+ " [https://twitter.com/Tillerinobot status and updates]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki commands]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Frequently asked questions]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, at this point " + feature + " is only available for players who have surpassed rank " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "What do you mean nomod with mods?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " I've recommended everything that I can think of]."
				+ " Try other recommendation options or use !reset. If you're not sure, check !help.";
	}

	@Override
	public String notRanked() {
		return "Looks like that beatmap is not ranked.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Invalid accuracy: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		/*
		 * TRANSLATION NOTE: This line is sent to the user right after they have
		 * chosen this Language implementation. The English version refers to
		 * itself as the default version ("just the way I am"), so translating
		 * the English message doesn't make any sense.
		 * 
		 * Instead, we've been using the line
		 * "*Translator* helped me learn *Language*." in translations. Replace
		 * *Translator* with your osu name and *Language* with the name of the
		 * language that you are translating to, and translate the line into the
		 * new language. This serves two purposes: It shows that the language
		 * was changed and gives credit to the translator.
		 * 
		 * You don't need to use the line above, and you don't have have to give
		 * yourself credit, but you should show that the language has changed.
		 * For example, in the German translation, I just used the line
		 * "Nichts leichter als das!", which translates literally to
		 * "Nothing easier than that!", which refers to German being my first
		 * language.
		 * 
		 * Tillerino
		 * 
		 * P.S. you can put a link to your profile into the line like this:
		 * [https://osu.ppy.sh/u/2070907 Tillerino]
		 */
		return new Message("So you like me just the way I am :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "I'm sorry, but \"" + invalid
				+ "\" does not compute. Try these: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "The syntax to set a parameter is !set option value. Try !help if you need more pointers.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "The osu! servers are super slow right now, so there's nothing I can do for you at this moment. ";
		return message + apiTimeoutShuffler.get(
				"Say... When was the last time you talked to your grandmother?",
				"How about you clean your room and then ask again?",
				"I bet you'd love to take a walk right now. You know... outside?",
				"I just know that you have a bunch of other things to do. How about just doing them now?",
				"You look like you need a nap anyway.",
				"But check out this super interesting page on [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
				"Let's check if anybody good is [http://www.twitch.tv/directory/game/Osu! streaming] right now!",
				"Look, here is another [http://dagobah.net/flash/Cursor_Invisible.swf game] that you probably suck at!",
				"This should give you plenty of time to study [https://github.com/Tillerino/Tillerinobot/wiki my manual].",
				"Don't worry, these [https://www.reddit.com/r/osugame dank memes] should pass the time.",
				"While you're bored, give [http://gabrielecirulli.github.io/2048/ 2048] a try!",
				"Fun question: If your harddrive crashed right now, how much of your personal data would be lost forever?",
				"So... Have you ever tried the [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"You can go do something else or we can just stare into each others eyes. Silently."
				);
	}

	@Override
	public String noRecentPlays() {
		return "I haven't seen you play lately.";
	}
	
	@Override
	public String isSetId() {
		return "This references a set of beatmaps, not a single beatmap.";
	}
}

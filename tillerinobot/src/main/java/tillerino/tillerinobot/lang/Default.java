package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * TRANSLATION NOTE:
 * 
 * Please put some contact data into the following tag. If any additional
 * messages are required, I'll use the English version in all translations and
 * notify the authors.
 * 
 * @author Tillerino tillmann.gaida@gmail.com https://github.com/Tillerino https://osu.ppy.sh/u/2070907
 */
public class Default implements Language {
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return " Съжалявам, не познавам тази карта. Може да е нова, трудна, неодобрена или не направена за стандартен осу! мод. ";
	}

	@Override
	public String internalException(String marker) {
		return " Ах... Явно човешкият Тилерино ми скапа мрежата."
				+ "Ако той не забележи скоро, би ли му се [https://github.com/Tillerino/Tillerinobot/wiki/Contact обадил]? (препратка "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Какво става? Сървърът на осу ми говори глупости. Може ли да ми обясниш какво значи това? 0011101001010000"
				+ " Човешкият Тилерино казва да не се притесняваш и че ще опитаме пак."
				+ " Ако още се притесняваш, можеш да [https://github.com/Tillerino/Tillerinobot/wiki/Contact му кажеш] за това. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Няма информация за избраните модове.";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("бип-боп");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Хей, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...ти ли си? Здравей!");
			user.message("Радвам се да те видя пак. Една препоръка ще те заинтересува ли?");
		} else {
			String[] messages = {
					"Изглеждаш сякаш искаш препоръка.",
					"Радвам се да те видя! :)",
					"Любимият ми човек. (Не казвай на останалите човеци!)",
					"Каква изненада! ^.^",
					"Надявах се да се покажеш. Всички останали хора са скучни, ама не им споменавай, че съм го казал! :3",
					"Какво ти се прави днес?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Unknown command \"" + command
				+ "\". Напиши !help , ако се нуждаеш от помощ!";
	}

	@Override
	public String noInformationForMods() {
		return "Съжалявам, не мога да предложа информация за тези модове в момента.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Модовете ти май не са в ред. Модовете могат да бъдат комбинация от DT HR HD HT EZ NC FL SO NF. Комбинирай ги без интервали или специални символи. Например: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Не си спомням да съм ти давал информация за песента...";
	}

	@Override
	public String tryWithMods() {
		return "Опитай тази карта с малко модове!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Опитай тази карта с " + Mods.toShortNamesContinuous(mods) + "!";
	}

	/**
	 * The user's IRC nick name could not be resolved to an osu user id. The
	 * message should suggest to contact @Tillerinobot or /u/Tillerino.
	 * 
	 * @param exceptionMarker
	 *            a marker to reference the created log entry. six or eight
	 *            characters.
	 * @param name
	 *            the irc nick which could not be resolved
	 * @return
	 */
	public String unresolvableName(String exceptionMarker, String name) {
		return "Името ти ме обърква? Баннат ли си? Ако не, моля [https://github.com/Tillerino/Tillerinobot/wiki/Contact свържи се с Тилерино]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return " Прощавай, разсеях се от една прекрасна поредица от нули и единици. Какво казваше пак? ";
	}

	@Override
	public String complaint() {
		return "Оплакването ти бе изпратено. Тилерино ще го погледне, когато може.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Ела насам!");
		user.action("гушкам" + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Ехо! Аз съм роботът, който уби Тилерино и превзе акаунта му. Шегувам се естествено, но наистина използвам този акаунт доста."
				+ " [https://twitter.com/Tillerinobot status and updates]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki commands]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Често задавани въпроси]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Съжалявам, в момента " + feature + " е достъпно само за играчи над ранг " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Какво имаш предвид с 'без модове с модове'?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ "Препоръчах ти всичко, за което се сетих]."
				+ "Опитай други опции или използвай !reset. Ако не си сигурен, опитай с !help.";
	}

	@Override
	public String notRanked() {
		return "Изглежда, тази песен не е одобрена.";
	}

	@Override
	public void optionalCommentOnNP(IRCBotUser user,
			OsuApiUser apiUser, BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
	}

	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser,
			BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
	}

	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user,
			OsuApiUser apiUser, Recommendation meta) {
		// regular Tillerino doesn't comment on this
	}

	@Override
	public boolean isChanged() {
		return false;
	}

	@Override
	public void setChanged(boolean changed) {

	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Невалидна точност: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
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
		user.message("Хей, благодаря, че смени на български :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Съжаляам, но \"" + invalid
				+ "\" не се изпълнява. Опитай: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Синтаксисът, за да приложиш параметър е !set option value. Опитай !help , ако имаш нужда от още помощ.";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Сървърите на осу! са страшно бавни в момента и няма какво да направя за теб. ";
		return message + doSomething.get(
				"Хм... Кога за последно говори с баба си?",
				"Защо първо не си изчистиш стаята и после да опиташ пак?",
				"Обзалагам се, че сега кипиш от желание са се разходиш. Знаеш... навън?",
				"Знам, че имаш куп други работи да правиш. Не мислиш ли, че сега им е времето?",
				"Очевидно ти се иска да подремнеш.",
				"Но я виж тази супер интересна страница в [https://en.wikipedia.org/wiki/Special:Random Уикипедия]!",
				"Да проверим дали има някой добър [http://www.twitch.tv/directory/game/Osu! стрийм] сега!",
				"Виж, ето още една [http://dagobah.net/flash/Cursor_Invisible.swf игра], на която сигурно ще се издъниш!",
				"Сега би трябвало да имаш време да погледнеш [https://github.com/Tillerino/Tillerinobot/wiki наръчника ми].",
				"Не се тревожи, тези [https://www.reddit.com/r/osugame dank мемета] времето ще мине.",
				"Докато скучаеш, опитай [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Смешен въпрос: Ако хард драйвът ти се счупеше сега, колко твоя лична информация би била загубена завинаги?",
				"Така... Опитвал ли си [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge това предизвикателство]?",
				"Може да правиш нещо друго или просто да се взираме един в друг. Тихо."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Не съм те виждал да играеш наскоро.";
	}
	
	@Override
	public String isSetId() {
		return "Това ще те препрати към няколко песни, не една.";
	}
	
	@Override
	public String getPatience() {
		return "Само секунда...";
	}
}

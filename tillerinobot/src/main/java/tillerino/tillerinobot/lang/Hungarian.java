package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author https://github.com/moriczgergo https://osu.ppy.sh/u/skiilaa
 */
public class Hungarian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Bocsi, de nem ismerem ezt a mapot. Lehet hogy nagyon új, nagyon nehéz, unranked, vagy nem osu!standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Pff... Úgylátszik hogy az ember Tillerino kihúzta a kábeleimet."
				+ " Ha nem veszi észre, akkor el tudnád mondani [https://github.com/Tillerino/Tillerinobot/wiki/Contact neki]? Köszi! (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Mi történik? Értelmetlen kódokat kapok az osu! szerveréről. Elmondanád hogy ez mit jelent: 0011101001010000?"
				+ " Az ember Tillerino azt mondja, hogy minden oké, és próbáld újra."
				+ " Ha valamilyen okból nagyon \"félsz\" akkor [https://github.com/Tillerino/Tillerinobot/wiki/Contact mondd el neki]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "nincs adat a modokról";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("bíp búp");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Üdv. újra, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...te vagy az? Olyan rég láttalak!"))
				.then(new Message("Jó hogy visszajöttél. Küldhetek egy ajánlást?"));
		} else {
			String[] messages = {
					"úgy látszik ajánlásra éhezel.",
					"jó látni! :)",
					"te vagy a kedvenc emberem. (Ne mondd el másnak!)",
					"milyen kedves meglepetés! ^.^",
					"Reméltem hogy visszajössz. Az összes többi ember unalmas és buta, de ezt ne mondd el nekik! :3",
					"hogy vagy ezen a csodás napon?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Ismeretlen parancs: \"" + command
				+ "\". Írd be azt hogy \"!help\" ha kiváncsi vagy a parancslistára!";
	}

	@Override
	public String noInformationForMods() {
		return "Bocsi, nem tudok erről a modokról semmit se. :V";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Ezek a modok összezavarnak. A modok bárhogy kombinálhatóak ezek közül: DT HR HD HT EZ NC FL SO NF. Írd le őket szóköz vagy speciális karakter(pl. vessző) nélkül. Például !with HDHR, vagy !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nem emlékszem hogy adtam volna neked bármilyen infót egy dalról... Ha kilépsz, újra meg kell kérdezned, erre vigyázz!";
	}

	@Override
	public String tryWithMods() {
		return "Próbáld ki ezt a mapot modokkal!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Próbáld ki ezt a mapot " + Mods.toShortNamesContinuous(mods) + " modokkal!";
	}

	@Override
	public String excuseForError() {
		return "Bocsi, itt volt ez a gyönyörű kód nullásokból és egyesekből, és elterelte a figyelmemet. Mit akartál?";
	}

	@Override
	public String complaint() {
		return "A jelentésedet elküldtem. Tillerino megnézi, amint tudja.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Gyere ide!")
			.then(new Action("megöleli " + apiUser.getUserName() + "t"));
	}

	@Override
	public String help() {
		return "Szia! Én vagyok a robot aki megölte Tillerinot és átvette a profilja felett a hatalmat. Csak viccelek, de sokat használom ezt a profilt."
				+ " [https://twitter.com/Tillerinobot státusz és frissítések]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki parancsok]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact üzenj]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ GY.I.K (Gyakran Ismételt Kérdések)]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Bocsi, de a " + feature + " szolgáltatás csak a " + minRank + " és nagyobb rankúaknak elérhető.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Mit akarsz itt a nomoddal és a modokkal? -,-";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Mindend elmondtam amire tudok gondolni]."
				+ " Ha új listát akarsz keérni használd a \"!reset\" parancsot. Ha nem vagy biztos benne, csekkold le a \"!help\"-et.";
	}

	@Override
	public String notRanked() {
		return "Úgy látszik ez a beatmap még nem rankolt.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Hamis pontosság: \"" + acc + "\"";
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
		return new Message("Kicsit nehéz volt megtanulni... de megcsináltam! (fordító: [https://osu.ppy.sh/u/BakaHarcos BakaHarcos], [https://osu.ppy.sh/u/10148621 skiilaa])");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Bocsi, de az hogy \"" + invalid
				+ "\" nem logikus. Próbáld meg: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "A parancs a paraméter meghatározására a !set. Példa: !set Language Hungarian. Kukkantsd meg a !help-et.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "Az osu! szerverek naaagggyyyooonnn lasssúúúaaakkk, úgyhogy most nem sok dolgot tudok csinálni. ";
		return message + apiTimeoutShuffler.get(
				"Mikor beszéltél utóljára a nagyiddal?",
				"Takarítsd ki a szobádat. Amire végzel, akkor hátha működik.",
				"Jó lenne kimenni sétálni, az elvinné az időt. Tudod...az utcán...",
				"Tudok EZER dolgot hogy mit tudnál csinálni. Miért nem csinálod meg most?",
				"Legalább most tudsz pihenni. Úgy nézel ki mind egy zombi.",
				"De kuksold meg ezt az érdekes oldalt a [https://hu.wikipedia.org/wiki/Special:Random wikipédián]!",
				"Nézd meg hogy valaki [http://www.twitch.tv/directory/game/Osu! streamel] e most! Biztos találsz valakit.",
				"Nézd, itt egy másik [http://dagobah.net/flash/Cursor_Invisible.swf játék] amiben garantálom hogy szar vagy!",
				"Ebben az időben átnézheted az [https://github.com/Tillerino/Tillerinobot/wiki útmutatómat]. Legalább nem fogsz értelmetlen parancsokat küldözgetni.",
				"Ne szomorkodj, ezek a [https://www.reddit.com/r/osugame hülye mémek] biztos elütik az időt.",
				"Amíg unatkozol, próbáld ki az [http://gabrielecirulli.github.io/2048/ 2048]-at. (Ez az üzenet termékmegjelenítést tartalmaz.)",
				"Vicces kérdés: Ha a merevlemezed meghalna, mennyi személyes adat veszne el? (Muhahahahaha...)",
				"Kipróbáltad már a [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up kihívás]t?",
				"Csinálhatsz valamit, vagy nézhetünk farkas-szemet. Csöndben."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Nem látlak mostanában játszani, és nagyon szomorúak a pp-id.";
	}
	
	@Override
	public String isSetId() {
		return "Ez egy beatmap set, és nem egy beatmap.";
	}
}


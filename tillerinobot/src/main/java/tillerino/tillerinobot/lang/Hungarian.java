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
		return "Bocsánat, de én nem ismerem ezt a mapot. Lehet hogy nagyon új, nagyon nehéz, rankolatlan vagy nem osu!standard map.";
	}

	@Override
	public String internalException(String marker) {
		return "Pff... Úgy látszik hogy az ember Tillerino kihúzta a kábeleimet."
				+ " Ha nem veszi észre hamarosan, [https://github.com/Tillerino/Tillerinobot/wiki/Contact fel tudnát keresni]? ("+marker+
				+ " referencia)";
	}

	@Override
	public String externalException(String marker) {
		return "Mi történik? Idétlen kódokat kapok az osu! szerveréről. Megmondanád, hogy ez mit jelentene? 0011101001010000"
				+ " Az ember Tillerino aztmondja, hogy semmi baj nincs, próbáld meg újra."
				+ " Ha nagyon megijedtél valamiért, [https://github.com/Tillerino/Tillerinobot/wiki/Contact mondd el neki]." + marker +"
				+ " referencia)";
	}

	@Override
	public String noInformationForModsShort() {
		return "nincs adat a modokról";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("bíp búp");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("De jó újra látni, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...te vagy az? De rég láttalak!");
			user.message("Jó hogy visszajöttél. Kérsz egy ajánlatot?");
		} else {
			String[] messages = {
					"úgy néz ki, hogy szeretnél egy ajánlatot.",
					"hogy milyen jó látni téged! :)",
					"te vagy a kedvenc emberem. (Ne mondd el a többi embernek!)",
					"milyen kedves megelepetés! ^.^",
					"Reméltem hogy visszajössz. Az összes többi ember unalmas és buta, de ne mondd el nekik hogy ezt mondtam! :3",
					"hogy vagy ma?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Nem ismerem ezt a parancsot: \"" + command
				+ "\". Írd be azt hogy !help ha a parancsok listájára vagy kiváncsi!";
	}

	@Override
	public String noInformationForMods() {
		return "Bocsi, nem ismerem ezeket a modokat. Nézz vissza később!";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Ömm... ez a modkombináció furán néz ki. A modokat bárhogy lehet kombinálni ezek közül: DT HR HD HT EZ NC FL SO NF. Kombináld őket szóköz, vagy speciális karakter(pl. vessző) nélkül. Példa: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nem emlékszem hogy adtam volna neked infót bármelyik dalról... Először menj rá a dalra, írd be azt hogy /np, majd a parancsot amit most beirtál.";
	}

	@Override
	public String tryWithMods() {
		return "Próbáld ki ezt a mapot modokkal!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Próbáld ki ezt a mapot " + Mods.toShortNamesContinuous(mods) + " modokkal!";
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
		return "A neved összezavar. Kibannoltak? Ha nem, légyszives írj [https://github.com/Tillerino/Tillerinobot/wiki/Contact Tillerino]-nak. ("+ exceptionMarker +
				+ " referencia)";
	}

	@Override
	public String excuseForError() {
		return "Bocsi, Itt volt ez a gyönyörű kód ami egyesekből és nullásokból állt és elkalandoztam. Mit akartál?";
	}

	@Override
	public String complaint() {
		return "A fellebezésed készen van. Tillerino meg fogja nézni, amint tudja.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Te! Gyere ide!");
		user.action("megöleli " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Szia! Én vagyok a robot aki megölte Tillerino-t és átvette a profilja felett a hatalmat. Csak viccelek, de nagyon sokat használom ezt a profilt."
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
		return "Bocsi, de most a " + feature + " szolgáltatás csak azoknak érhető el akinek legalább ennyi rankja van:" + minRank;
	}

	@Override
	public String mixedNomodAndMods() {
		return "Mit akartál mondani a nomoddal és a modokkal?";
	}

	@Override
	public String outOfRecommendations() {
		return "Mindent elmondtam amit csak tudtam."
				+ " Próbálj meg más beállításokkal ajánlásokat keresni, vagy használd a !reset parancsot. Ha nem vagy benne biztos, csekkold le az útmutatót: !help.";
	}

	@Override
	public String notRanked() {
		return "Azthiszem ez a beatmap nem ranked. És hát ki akarna nem ranked beatmapból pp-t szerezni...";
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
		return "Hamis pontosság: \"" + acc + "\"";
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
		user.message("[https://osu.ppy.sh/u/BakaHarcos BakaHarcos]([http://github.com/ApeConfirmed ApeConfirmed]) megtanított magyarul beszélni. :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Bocsi, de a(z) \"" + invalid
				+ "\" nem értelmes. Próbáld meg ezeket: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "A parancs a paraméter megadására a !set. Kukkantsd meg a !help-et ha nem vagy benne biztos";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Az osu! serverek naagggyyyooooonnnn beeellaaaasssuuulltttaak, szóval most nem tudok semmit se csinálni. :V ";
		return message + doSomething.get(
				"Mikor beszéltél utoljára a nagyiddal?",
				"Addig takarítsd ki a szobád. Hátha addigra jó lesz.",
				"Jó lenne egyet sétálni most. Tudod...az udvarra?",
				"Tudok egy ccssommmóóóóó dolgot amit lehetne csinálni. Miért nem csinálod meg őket most?",
				"Úgy nézel ki, mind egy zombi. Inkább tégy egy pihenőt.",
				"De csekkold le ezt az érdekes oldalt a [https://hu.wikipedia.org/wiki/Special:Random wikipédián]!",
				"Cseekold le hogy valaki [http://www.twitch.tv/directory/game/Osu! streamel] e most. Biztos vagyokv benne, hogy találsz valamit",
				"Nézd, itt egy másik [http://dagobah.net/flash/Cursor_Invisible.swf játék] amiben bisztos hogy szar vagy!",
				"Legalább lesz egy kis idő, hogy meg nézd az [https://github.com/Tillerino/Tillerinobot/wiki útmutatómat], hogy abbahagyd a hülye parancsok küldözgetését.",
				"Ne búsulj, ezek a [https://www.reddit.com/r/osugame hülye mémek] biztos elviszik az időt.",
				"Amíg unatkozol, próbáld ki a [http://gabrielecirulli.github.io/2048/ 2048]-at!",
				"Vicces kérdés: Ha a merevlemezed bekrepálna, mennyi saját adat veszne el? (Muhahahahaa...)",
				"Szóvaall... Kipróbáldat már a [https://www.google.hu/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up kihívást]?",
				"Elmehetsz valami mást csinálni, vagy nézhetünk egymás szemébe folyamatosan. Halkan."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Nem láttalak játszani mostanában.";
	}
	
	@Override
	public String isSetId() {
		return "Ez egy beatmap set, nem egy beatmap.";
	}
	
	@Override
	public String getPatience() {
		return "Csak egy pillanat... Várom hogy Tillerino megszerelje már azt a netkábelt...";
	}
}

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
		return "Bocsanat, de en nem ismerem ezt a mapot. Lehet hogy nagyon uj, nagyon nehez, rankolatlan vagy nem osu!standard map.";
	}

	@Override
	public String internalException(String marker) {
		return "Pff... Ugy látszik hogy az ember Tillerino kihuzta a kabeleimet."
				+ " Ha nem veszi eszre hamarosan, [https://github.com/Tillerino/Tillerinobot/wiki/Contact fel tudnad keresni]? ("+marker+
				+ " referencia)";
	}

	@Override
	public String externalException(String marker) {
		return "Mi tortenik? Idetlen kodokat kapok az osu! szervererol. Megmondanad, hogy ez mit jelentene? 0011101001010000"
				+ " Az ember Tillerino aztmondja, hogy semmi baj nincs, probald meg ujra."
				+ " Ha nagyon megijedtel valamiert, [https://github.com/Tillerino/Tillerinobot/wiki/Contact mondd el neki]." + marker +
				+ " referencia)";
	}

	@Override
	public String noInformationForModsShort() {
		return "nincs adat a modokrol";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("bip bup");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("De jo ujra latni, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...te vagy az? De reg lattalak!");
			user.message("Jo hogy visszajottel. Kérsz egy ajanlatot?");
		} else {
			String[] messages = {
					"ugy nez ki, hogy szeretnel egy ajanlatot.",
					"hogy milyen jo latni teged! :)",
					"te vagy a kedvenc emberem. (Ne mondd el a tobbi embernek!)",
					"milyen kedves meglepetes! ^.^",
					"Remeltem hogy visszajossz. Az osszes többi ember unalmas es buta, de ne mondd el nekik hogy ezt mondtam!",
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
				+ "\". Ird be azt hogy !help ha a parancsok listajara vagy kivancsi!";
	}

	@Override
	public String noInformationForMods() {
		return "Bocsi, nem ismerem ezeket a modokat. Nézz vissza kesobb!";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Omm... ez a modkombinacio furan nez ki. A modokat barhogy lehet kombinalni ezek kozul: DT HR HD HT EZ NC FL SO NF. Kombinald oket szokoz, vagy specialis karakter(pl. vesszo) nelkul. Pelda: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nem emlekszem hogy adtam volna neked infot bármelyik dalról... Először menj ra a dalra, ird be azt hogy /np, majd a parancsot amit most beirtal.";
	}

	@Override
	public String tryWithMods() {
		return "Probald ki ezt a mapot modokkal!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Probald ki ezt a mapot " + Mods.toShortNamesContinuous(mods) + " modokkal!";
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
		return "A neved osszezavar. Kibannoltak? Ha nem, legyszives irj [https://github.com/Tillerino/Tillerinobot/wiki/Contact Tillerino]-nak. ("+ exceptionMarker +
				+ " referencia)";
	}

	@Override
	public String excuseForError() {
		return "Bocsi, de itt volt ez a gyonyoru kod ami egyesekbol és nullasokbol allt es elkalandoztam. Mit akartal?";
	}

	@Override
	public String complaint() {
		return "A fellebezesed keszen van. Tillerino meg fogja nezni, amint tudja.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Te! Gyere ide!");
		user.action("megoleli " + apiUser.getUserName() + "t");
	}

	@Override
	public String help() {
		return "Szia! En vagyok a robot aki megolte Tillerino-t es atvette a profilja felett a hatalmat. Csak viccelek, de nagyon sokat hasznalom ezt a profilt."
				+ " [https://twitter.com/Tillerinobot statusz és frissitesek]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki parancsok]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact uzenj]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ GY.I.K (Gyakran Ismetelt Kerdesek)]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Bocsi, de most a " + feature + " szolgaltatas csak azoknak erheto el akinek legalabb ez a rankja/ennyi ppje van:" + minRank;
	}

	@Override
	public String mixedNomodAndMods() {
		return "Mit akartal mondani a nomoddal es a modokkal?";
	}

	@Override
	public String outOfRecommendations() {
		return "Mindent elmondtam amit csak tudtam."
				+ " Próbalj meg más beallitasokkal ajanlasokat keresni, vagy hasznald a !reset parancsot. Ha nem vagy benne biztos, csekkold le az utmutatot: !help.";
	}

	@Override
	public String notRanked() {
		return "Azthiszem ez a beatmap nem ranked. Es hat ki akarna nem ranked beatmapbol pp-t szerezni...";
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
		return "Hamis pontossag: \"" + acc + "\"";
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
		user.message("[https://osu.ppy.sh/u/BakaHarcos BakaHarcos]([http://github.com/ApeConfirmed ApeConfirmed]) megtanitott magyarul beszelni. :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Bocsi, de a(z) \"" + invalid
				+ "\" nem ertelmes. Próbáld meg ezeket: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "A parancs a parameter megadasara a !set. Kukkantsd meg a !help-et ha nem vagy benne biztos";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Az osu! serverek naagggyyyooooonnnn beeellaaaasssuuulltttaak, szoval most nem tudok semmit se csinalni. ";
		return message + doSomething.get(
				"Mikor beszeltel utoljara a nagyiddal?",
				"Addig takarítsd ki a szobad. Hatha addigra jo lesz.",
				"Jo lenne egyet setalni most. Tudod... az utcan.",
				"Tudok egy ccssommmooo dolgot amit lehetne csinalni. Miért nem csinalod meg oket most?",
				"Ugy nezel ki, mind egy zombi. Inkabb tegy egy pihenot.",
				"De csekkold le ezt az erdekes oldalt a [https://hu.wikipedia.org/wiki/Special:Random wikipedian]!",
				"Csekold le hogy valaki [http://www.twitch.tv/directory/game/Osu! streamel] e most. Biztos vagyok benne, hogy talalsz valamit.",
				"Nezd, itt egy masik [http://dagobah.net/flash/Cursor_Invisible.swf jatek] amiben biztos hogy szar vagy!",
				"Legalabb lesz egy kis ido, hogy meg nezd az [https://github.com/Tillerino/Tillerinobot/wiki utmutatomat], hogy abbahagyd a hulye parancsok kuldozgeteset.",
				"Ne busulj, ezek a [https://www.reddit.com/r/osugame hulye memek] biztos elviszik az idot.",
				"Amig unatkozol, próbáld ki a [http://gabrielecirulli.github.io/2048/ 2048]-at!",
				"Vicces kerdes: Ha a merevlemezed bekrepalna, mennyi sajat adat veszne el? (Muhahahahaa...)",
				"Szovaaalll... Kiprobaltad mar a [https://www.google.hu/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up kihivast]?",
				"Elmehetsz valami mast csinálni, vagy nezhetunk egymas szemebe folyamatosan. Halkan."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Nem lattalak jatszani mostanaban.";
	}
	
	@Override
	public String isSetId() {
		return "Ez egy beatmap set, nem egy beatmap.";
	}
	
	@Override
	public String getPatience() {
		return "Csak egy pillanat... Varom hogy Tillerino megszerelje mar azt a netkabelt...";
	}
}

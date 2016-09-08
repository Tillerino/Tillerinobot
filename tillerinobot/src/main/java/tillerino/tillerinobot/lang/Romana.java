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
 * @author Dddsasul ffrrtt223@gmail.com https://github.com/Dddsasul https://osu.ppy.sh/u/6579055
 */
public class Romana implements Language {
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Îmi pare rău, nu cunosc acea hartă. S-ar putea să fie foarte nouă, foarte grea, mod necompetitiv sau să nu fie în modul osu standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Umm... Se pare că Tillerino omul, mi-a stricat mesajul."
				+ " Daca nu observă în curând, ai putea să [https://github.com/Tillerino/Tillerinobot/wiki/Contact îl informezi]? (referinîță "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Ce se întâmplă? primesc numai aberații de la serverul OSU. Poți să îmi spui ce ar trebui să însemne asta? 0011101001010000"
				+ " Tillerino omul spune că nu e ceva de îngrijorat, și că ar trebui să încercăm din nou."
				+ " Dacă esti foarte îngrijorat dintr-un motiv anume, poți sa îi [https://github.com/Tillerino/Tillerinobot/wiki/Contact spui] despre asta. (referință "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "nu există date pentru modurile cerute";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Bun venit înapoi, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...ești chiar tu? A trecut atât de mult timp!");
			user.message("Imi pare bine ca ai revenit. Pot să îți dau o recomandare?");
		} else {
			String[] messages = {
					"arăți ca și cum ai vrea o recomandare.",
					"îmi pare bine să te văd! =]",
					"omul meu favorit. (Nu le spune celorlalți oameni!)",
					"ce surpriză plăcută! ^.^",
					"Speram săapari. Toti ceilalti oameni sunt jalnici, dar nu lespune că am zis asta! :3",
					"ce vrei să faci azi?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Comandă necunoscută \"" + command
				+ "\". Scrie !help dacă ai nevoie de ajutor!";
	}

	@Override
	public String noInformationForMods() {
		return "îmi pare rău, nu pot da informații despre aceste moduri momentan.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Aceste moduri nu arată bine. Modurile pot fi orice combinație de DT HR HD HT EZ NC FL SO NF. Combina-le fara spații sau caractere speciale. Exemplu: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nu îmi amintesc să iți fi dat informații despre vreun cântec...";
	}

	@Override
	public String tryWithMods() {
		return "Încearcă harta asta cu niște moduri!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Încearcă harta asta cu " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Îmi pare rău, mă uitam la această minunată segvență de unu și zero și n-am mai fost atent. Ce ai vrut?";
	}

	@Override
	public String complaint() {
		return "Plângerea ta a fost trimisă. Tillerino se va uita pe ea când poate.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Vino aici, tu!");
		user.action("îl îmbrățișează pe " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Bună! Sunt robotul care l-a omorât pe Tillerino și i-a preluat contul. Doar glumeam, dar îi folosesc contul mult."
				+ " [https://twitter.com/Tillerinobot status și actualizări]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki comenzi]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Întrebări frecvente]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Îmi pare rău, momentan " + feature + " este valabil(ă) numai pentru jucătorii care au trecut de rank-ul " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Cum adicaă fără moduri cu moduri?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Am recomandat tot ce mi-a trecut prin cap]."
				+ " Încearcă alte opțiuni de recomandări sau încearcă !reset. Dacă nu ești sigur, verifică !help.";
	}

	@Override
	public String notRanked() {
		return "Se pare că acea hartă nu este în modul competitiv.";
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
		return "Acuratețe incalidă: \"" + acc + "\"";
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
		user.message("[https://osu.ppy.sh/u/6579055 Dddsasul] mă ajută să învăț Româna!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Îmi pare rău, dar \"" + invalid
				+ "\" nu poate fi procesat. Încearcă: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Pentru a seta un parametru folosește !set opțiune valoare. Încearcă !help dacă ai nevoie de mai multe indicații.";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Serverele osu! se mișcă foarte greu, nu te pot ajuta momentan =[. ";
		return message + doSomething.get(
				"Fi sincer... Când a fost ultima oară când ai vorbit cu bunica ta?",
				"Ce-ar fi să-ți cureți camera și sa întrebi după?",
				"Pun pariu că ți-ar placea să te plimbi puțin. Ști tu... afară?",
				"Știu sigur că ai niște lucruri de făcut. Ce-ar fi să le faci acum?",
				"Oricum arăți de parcă mai ai puțin și adormi.",
				"Dar verifică această pagina mega interesantă [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
				"Hai să verificam dacă este vreun [http://www.twitch.tv/directory/game/Osu! streamer] bun online!",
				"Uite, acesta este un alt [http://dagobah.net/flash/Cursor_Invisible.swf joc] la care nu te pricepi!",
				"Asta ar trebui să îți dea suficient timp să îmi citești [https://github.com/Tillerino/Tillerinobot/wiki manualul].",
				"Nu îți face griji, cu aceste aceste [https://www.reddit.com/r/osugame meme-uri dank] ar trebui să treacă timpul.",
				"Dacă ești plictisit, încearcă [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Întrebare amuzantă: Dacă ți s-ar strica hardul acuma, câte din datele tale personale s-ar pierde pentru totdeauna?",
				"Deci... Ai încercat vreodată [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge provocarea de flotări Sally]?",
				"Poți merge să faci altceva sau am putea să... ne uităm unul în ochii celuilalt, în liniște."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return new Default().noRecentPlays();
	}
	
	@Override
	public String isSetId() {
		return new Default().isSetId();
	}
	
	@Override
	public String getPatience() {
		return new Default().getPatience();
	}
}

package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;

/**
 * @author Haganenno https://osu.ppy.sh/u/4692344 https://github.com/peraz
 */
public class Lithuanian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();
	
	@Override
	public String unknownBeatmap() {
		return "Atsiprašau, bet tokio beatmapo nežinau. Galbūt jis yra naujas, labai sunkus, nepatvirtintas arba ne osu!standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Velnias... mano kūrėjai susimovė."
				+ " Jeigu jie artimiausiu metu to nepastebės ir nepataisys, gal galėtum apie [https://github.com/Tillerino/Tillerinobot/wiki/Contact tai pranešti]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Nesuprantu kas darosi? osu! servas kažkokias nesąmones siunčia. Ar galėtum pasakyti, ką tai reiškia? 0011101001010000."
				+ " mano kūrėjai sako, kad čia nieko rimto ir siūlo mėginti dar kartą."
				+ " Jei manai, kad čia bus kažkas rimčiau gali [https://github.com/Tillerino/Tillerinobot/wiki/Contact mus informuoti]?. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Nėra duomenų su pateiktais modais.";
	}

	@Override
	public GameChatResponse welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Labas, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...Ar čia tikrai tu?"))
				.then(new Message("Labas. Gal kažką parekomenduoti?"));
		} else {
			String[] messages = {
					"atrodo, kad tau reikia rekomendacijų.",
					"smagu tave matyti! :)",
					"mano mėgstamiausias žmogus! (Tik nesakyk to kitiem!)",
					"ooo nustebinai! ^.^",
					"laukiau kol pasirodysi. Kiti mirtingieji mane nervina, tik nesakyk to kitiem! :3",
					"ką ruošiesi šiandien veikti?",
			};
			
			String message = messages[rnd.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "nežinoma komanda \"" + command
				+ "\". Rašyk !help, jei reikia pagalbos!";
	}

	@Override
	public String noInformationForMods() {
		return "Atsiprašau, bet neturiu info su šiais modais.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Tikriausiai ne taip suvedei modus. Vedant modus, gali rašyti bet kokią kombinaciją su šiais modais: DT HR HD HT EZ NC FL SO NF. Vesk juos be tarpų, pvz.: !with DTEZ, !with HDHR.";
	}

	@Override
	public String noLastSongInfo() {
		return "Aha... O kokiam čia mapui?";
	}

	@Override
	public String tryWithMods() {
		return "Pabandyk šį mapą su kokiais nors modais!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Pabandyk šitą su " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Atsiprašau, buvau paklydęs tarp nulių ir vienetų. Gali pakartot ko norėjai?";
	}

	@Override
	public String complaint() {
		return "Tavo skundas pateiktas. Tillerino peržiūrės kai prie jo prieis.";
	}

	@Override
	public GameChatResponse hug(OsuApiUser apiUser) {
		return new Message("Pss, eikš!")
			.then(new Action("apkabina " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Labas! Aš esu botas, kuris pagrobė Tillerino ir užgrobė jo accountą. Che che, juokauju. Bet šiaip tai rimtai naudojuosi šituo accountu."
				+ " [https://twitter.com/Tillerinobot būsena ir atnaujinimai]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki komandos]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontaktai]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Dažniausiai užduodami klausimai]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Atsiprašau, bet " + feature + " yra prieinama tik žaidėjams, kurie yra pasiekę bent " + minRank + " vietą.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Kaip suprast nomod su modais? Tai su ar be modu?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Pasiūliau, viską, ką tau galiu pasiūlyt]."
				+ " Mėgink kitus modus, nustatymus arba rašyk !reset. Jei nežinai, rašyk !help.";
	}

	@Override
	public String notRanked() {
		return "Šitas mapas dar nėra rankintas.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Neteisingai nurodytas tikslumas: \"" + acc + "\"";
	}

	@Override
	public GameChatResponse optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Haganenno su SlowLogicBoy išmokė mane lietuviškai :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Atsiprašau, bet \"" + invalid
				+ "\" nesiskaito, pabandyk " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Komanda, kuri nustato parametrą yra !set nustatymo reikšmė. Pabandyk !help, jeigu reikia daugiau patarimų. ";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "osu! servai lagina ir dabar niekuo negaliu padėt. ";
		return message + apiTimeoutShuffler.get(
				"Gal susitvarkom kambarį ir pamėginam vėliau?",
				"Manau reiktų pasivaikščiot arba pasivažinėt? Žinai... į lauką?",
				"Manau, kad turi darbų kuriuos reiktų padaryt. Davai pasidarom bent vieną dabar?",
				"Nenori dabar nusnūst?",
				"Tuo tarpu pasiskaitom [https://lt.wikipedia.org/wiki/Special:Random šitą] iš Viki!",
				"Reiktų pažiūrėt ar kas [http://www.twitch.tv/directory/game/Osu! streamina] osu!",
				"Pažiūrėk gal kažko nežinai [https://github.com/Tillerino/Tillerinobot/wiki apie mane].",
				"Be baimės, [https://www.reddit.com/r/osugame čia] rasim kaip prastumt laiką.",
				"Ė, žie, davai pažiūrim ką kiti [https://osu.ppy.sh/forum/t/117708 lietuviai] šneka forume?",
				"Eik ką nors kitą nuveik, bo mudu čia sėdėsim nieko neveikdami."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Pažaisk kažkiek.";
	}
	
	@Override
	public String isSetId() {
		return "Man reikia linko į konkretų mapą, o ne visą packą.";
	}
}

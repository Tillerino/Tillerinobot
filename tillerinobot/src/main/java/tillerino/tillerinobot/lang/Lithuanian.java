package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Haganenno https://osu.ppy.sh/u/4692344 https://github.com/peraz
 */
public class Lithuanian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();
	
	@Override
	public String unknownBeatmap() {
		return "Atsiprašau, aš nežinau šito grajaus. Jis yra naujas, labai sunkus, nepatvirtintas arba ne standartiniame Osu žaidimo režime.";
	}

	@Override
	public String internalException(String marker) {
		return "Blemba... žmogus Tillerino sujaukė mano laidus."
				+ " Jeigu jis artimiausiu metu to nepastebės, ar gali apie tai [https://github.com/Tillerino/Tillerinobot/wiki/Contact jį informuoti]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Kas darosi? Aš gaunu tik nesąmones iš Osu! serverio. Ar galėtum pasakyti, ką tai reiškia? 0011101001010000."
				+ " Žmogus Tillerino sako, kad nėra dėl ko nerimauti ir siūlo pabandyti dar kartą."
				+ " Jeigu dėl kažkokios priežasties jaudiniesi, gal gali [https://github.com/Tillerino/Tillerinobot/wiki/Contact jį informuoti]?. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Nėra duomenų apie pateiktus modus.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Sveikas sugrįžęs, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...ar čia tu? Šimtas metų!"))
				.then(new Message("Gera tave vėl matyti. Ar galiu tave sudominti pasiūlymu?"));
		} else {
			String[] messages = {
					"atrodo, kad tu nori rekomendacijos.",
					"kaip malonu tave matyti! :)",
					"mano mėgstamiausias žmogus. (Nesakyk kitiems žmonėms!)",
					"koks malonus siurprizas! ^.^",
					"ikėjausi, jog pasirodysi. Kiti žmonės mane užknisa, bet nesakyk, jog aš tai sakiau! :3",
					"ką ruošiesi šiandien veikti?",
			};
			
			String message = messages[rnd.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "nežinoma komanda \"" + command
				+ "\". Įvesk !help, jei nori pagalbos!";
	}

	@Override
	public String noInformationForMods() {
		return "Atleisk, dabar negaliu pasidalinti informacija apie šiuos modus.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Turbūt ne taip suvedei šiuos modus. Įvedant modus, gali įrašyti bet kokią kombinaciją šių modų: DT HR HD HT EZ NC FL SO NF. Rašyk juos kartu be jokių tarpų, pvz.: !with DTEZ, !with HDHR.";
	}

	@Override
	public String noLastSongInfo() {
		return "Nepamenu, jog tu būtum manęs paklausęs informacijos apie kokią dainą...";
	}

	@Override
	public String tryWithMods() {
		return "Pabandyk šį grajų su kokiais nors modais!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Pabandyk šį grajų su " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Atleisk, radau grąžų sakinį, sudaryta iš nulių ir vienetų ir aš išsiblaškiau. Pakartok dar kartą, ko norėjai?";
	}

	@Override
	public String complaint() {
		return "Tavo nusiskundimas buvo pateiktas. Tillerino jį peržiūrės, kai turės laiko.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Ei, tu, ateik čia!")
			.then(new Action("apkabina " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Sveikas! Aš esu robotas, kuris nužudė Tillerino ir pavogė jo paskyrą. Nepergyvenk, aš tik juokauju, bet aš tikrai dažnai naudojuosi šia paskyra."
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
		return "Atsiprašau, šiuo metu " + feature + " yra prieinama žaidėjams, kurie yra pasiekę " + minRank + " vietą.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Ką tu turi omeny, sakydamas nomod su modais?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Aš pasiūliau, viską, ką galiu]."
				+ " Pabandyk kitus pasiūlų pasirinkimus arba naudok !reset funkciją. Jei neesi tikras, rašyk !help.";
	}

	@Override
	public String notRanked() {
		return "Panašu, jog šis grajus dar nėra patvirtintas.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Negalimas tikslumas: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Haganenno išmokė mane lietuvių kalbos :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Atleisk, bet \"" + invalid
				+ "\" nesiskaito, pabandyk " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Komanda, kuri nustato parametrą yra !set nustatymo reikšmė. Pabandyk !help, jeigu tau reikia daugiau patarimų. ";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "osu! serveriai labai sulėtėjo ir šiuo momentu niekuo negaliu padėt. ";
		return message + apiTimeoutShuffler.get(
				"Gal susitvarkom kambarį ir pamėginam vėliau?",
				"Manau reiktų išeit pasivaikščiot arba pasivažinėt. Žinai... į lauką?",
				"Esu įsitikinęs, kad turi darbų kuriuos tu turi atlikt. Davai pasidarom bent vieną dabar?",
				"Žinai, šiuo metu tau reiktų nusnūst.",
				"Tuo tarpu paskaitom [https://lt.wikipedia.org/wiki/Special:Random šitą] iš Vikipedijos!",
				"Reiktų pažiūrėt ar kas įdomesnio [http://www.twitch.tv/directory/game/Osu! streamina] osu!",
				"Pažiūrėk gal kažko nežinai [https://github.com/Tillerino/Tillerinobot/wiki apie mane].",
				"Be baimės, [https://www.reddit.com/r/osugame čia] rasim kaip pratempt laiką.",
				"Ė, žie, davai pažiūrim ką kiti [https://osu.ppy.sh/forum/t/117708 mūsiškiai] šneka forume?",
				"Eik ką nors kitą nuveik, bo kitaip mudu čia sėdėsim tyliai nieko neveikdami."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Pažaisk kažkiek.";
	}
	
	@Override
	public String isSetId() {
		return "Man reikia nuorodos į konkretų sudėtingumą, o ne visą packą.";
	}
}

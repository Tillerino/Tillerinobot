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
 * @author Akuma Kudasai aka _BibleThump diabloxx50@gmail.com https://git.blosu.net/Diabloxx osu account is restricted
 */
public class Norwegian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Beklager. Jeg kjenner ikke til mappet. Det kan være den er ny, veldig vanskelig, unranked eller ikke er en standard osu map.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Ser ut som menneske Tillerino har tullet med kablene."
				+ " Hvis han ikke legger merke til det snart, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact informere han]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Hva er det som skjer? Jeg får bare tull tilbake fra osu servere. Kan du fortelle meg hva dette skal bety? 0011101001010000"
				+ " Mennekelige Tillerino sier at det er ingenting å beskymre seg for. Bare prøv på nytt."
				+ " Hvis du er skikkelig beskymret for en eller annen grunn, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact fortelle ham] om det. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Ingen data for modden du spurte om.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Velkommen tilbake, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...Er det deg? Hvor har du vært? Det er lenge siden!"))
				.then(new Message("Det er godt å ha deg tilbake. Kan jeg tilfredstille deg i en anbefaling fra meg?"));
		} else {
			String[] messages = {
					"det ser ut som du vil ha en anbefaling fra meg.",
					"så godt å se deg! :)",
					"du er mitt favoritt menneske. (Ikke fortell det til de andre!)",
					"for en hyggelig overraskelse! ^.^",
					"jeg håpte på at du skulle komme. Alle de andre er så kjedelige, men ikke fortell de at jeg sa det! :3",
					"hva føler du for å gjøre idag?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Unkjent Kommando \"" + command
				+ "\". Skriv !help om du trenger hjelp.";
	}

	@Override
	public String noInformationForMods() {
		return "Beklager, Jeg kan ikke hente informasjon for disse modsene ved dette øyeblikket.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Modsene du ga meg ser ikke riktig ut. Mods du kan kombinere er DT HR HD HT EZ NC FL SO NF. Kombiner dem uten mellomrom eller noen spesielle tegn. Eksempel: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Jeg husker ikka at jeg har gitt deg noen form for informasjon på sangen...";
	}

	@Override
	public String tryWithMods() {
		return "Prøv denne sangen med noen mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prøv denne sangen med " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Beklager, det var en fin sekvens med enere og nullere så jeg ble litt distrahert. Hva var det du spurte om igjen?";
	}

	@Override
	public String complaint() {
		return "Din klage har blitt sendt. Tillerino vil se på den så snart han har mulighet.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Du! Kom her!")
			.then(new Action("klem " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hei! Jeg er en robot som drepte Tillerino og tok over kontoen hannes. Bare tuller med deg, men jeg bruker kontoen ofte selv."
				+ " [https://twitter.com/Tillerinobot Status og oppdateringer]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki Kommandoer]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Ofte stilte spørsmål]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Beklager, for øyeblikket er " + feature + " bare tilgjengelig for spillere som har nådd rank: " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Hva mener du med nomod med mods?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Jeg har anbefalt alt jeg kan tenke over.]."
				+ " Prøv andre anbefalinger eller !reset. Hvis du er usikker, skjekk !help.";
	}

	@Override
	public String notRanked() {
		return "Ser ut som mappen ikke er ranked.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Unkjent accuracy: \"" + acc + "\"";
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
		return new Message("_BibleThump hjalp meg til å lære norsk.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Beklager, men \"" + invalid
				+ "\" kan ikke beregnes. Prøv disse: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaksen for å sette parameter er !set option value. Prøv !help hvis du trenger mer veiledning.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "osu! serverene er ikke så raske akkurat nå, så det er ingenting jeg kan gjøre for deg i dette øyeblikket. ";
		return message + apiTimeoutShuffler.get(
				"Fortell... Når sist snakket du med bestemoren din?",
				"Hva med at du rydder rommet ditt så spør du meg igjen?",
				"Jeg vedder på at du ville elsket å gått en tur akkuratt nå. Du vet... ut?",
				"Jeg bare vet at du har utrolig mye andre ting å gjøre. Hva med at du gjør dem nå?",
				"Det ser du som du trenger litt søvn uansett.",
				"Men skjekk ut denne super interresange siden på [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
				"La oss se om det er noen gode spillere som [http://www.twitch.tv/directory/game/Osu! streamer] akkurat nå!",
				"Se, her er et annet [http://dagobah.net/flash/Cursor_Invisible.swf spill] som du mest sansynlig kommer til å være dårlig i!",
				"Dette bør gi deg mye tid til å studere [https://github.com/Tillerino/Tillerinobot/wiki manualen min].",
				"Ikke beskymre deg! Disse [https://www.reddit.com/r/osugame danke memsene] bør få tiden til å fly.",
				"Mens du kjeder deg, gi [http://gabrielecirulli.github.io/2048/ 2048] et forsøk!",
				"Morsomt spørsmål: Hvis harddisken din krasjet akkurat nå, hvor mye av din personlige data vil bli tapt for altid?",
				"Så... Har du noen gang prøvd [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"Du kan gjøre noe annet eller vi kan stirre inn i øynene til hverandre mens vi er stille."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Jeg har ikke sett deg spille i det siste.";
	}
	
	@Override
	public String isSetId() {
		return "Denne refererer til et set med maps ikke en enkelt map.";
	}
}

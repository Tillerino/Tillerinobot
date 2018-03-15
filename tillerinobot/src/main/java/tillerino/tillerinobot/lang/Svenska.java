package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author https://osu.ppy.sh/u/3258429 SnickarN https://github.com/SnickarN-
 */
public class Svenska extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Förlåt, Jag känner inte till denna map. Den kan vara väldigt ny, väldigt svår, orankad eller inte osu!standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Ser ut som att den humana Tillerino skrev fel."
				+ " Om han inte märker inom kort, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact informera han]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Vad händer? Jag får bara tillbaka massa strunt från osu!servern. Kan du berätta för mig vad detta betyder? 0011101001010000"
				+ " Humana Tillerino säger att detta är inget att bli orolig över, och att vi borde försöka igen."
				+ " Om du är väldgt orolig av någon anledning, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact berätta det] för han. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Ingen data för efterfrågade modifikationer.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Välkommen tillbaka, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...Är det du? Det var så längesen!"))
				.then(new Message("Det känns bra att du är tillbaka. Kan jag intressera dig med en rekommendation?"));
		} else {
			String[] messages = {
					"det ser ut som att du vill ha en rekommendation.",
					"vad trevligt att ses! :)",
					"min favoritmänniska. (berätta inte för andra människor!)",
					"vilken trevlig överraskning! ^.^",
					"Jag hoppades på att du skulle visa dig. Alla andra människor är tråkiga, men berätta inte för dem att jag sa det! :3",
					"Vad känner du för att göra idag?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Ogiltigt kommando \"" + command
				+ "\". Skriv !help om du behöver hjälp!";
	}

	@Override
	public String noInformationForMods() {
		return "Förlåt, Jag kan inte ge dig information för dom modifikationera just nu.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Dom modifikationera ser inte rätt ut. Modifikationer kan vara någon kombination av DT HR HD HT EZ NC FL SO NF. Kombinera dem utan blanksteg eller specialkaraktärer. Exempel: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Jag kommer inte ihåg att du gett mig någon låtinfo...";
	}

	@Override
	public String tryWithMods() {
		return "Testa denna map med några modifikationer!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Testa denna map med " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Förlåt,  det var denna vackra sekvens av ettor och nollor och jag blev distraherad. Vad ville du nu igen?";
	}

	@Override
	public String complaint() {
		return "Ditt klagomål har lämnats in. Tillerino ska ta en närmare titt när han kan.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Kom hit, du!")
			.then(new Action("kram " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hej! Jag är roboten som dödade Tillerino och tog över hans konto. Skoja bara, men jag använder kontot mycket."
				+ " [https://twitter.com/Tillerinobot statusar och uppdateringar]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki kommandon]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ FAQ]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Förlåt, just nu är " + feature + "  bara tillgängligt för spelare som överträffat rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Vad menar du med nomod och mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Jag har rekommenderat allt jag kan tänka mig]."
				+ " Testa andra rekommendationsinställningar eller använd !reset. Om du är osäker, kolla !help.";
	}

	@Override
	public String notRanked() {
		return "Ser ut som att den mappen inte är rankad.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Ogiltig accuracy: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
        return new Message("[https://osu.ppy.sh/u/3258429 SnickarN] och [https://osu.ppy.sh/u/8116579 Padnezz] hjälpte mig att lära mig svenska!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Förlåt, men \"" + invalid
				+ "\" beräknas inte. Testa dessa: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaxen för att ställa en parameter är !set. Testa !help om du behöver mer hjälp.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "osu! servrarna är jätte sakta just nu, så jag kan inte göra något för dig just nu. ";
        return message + apiTimeoutShuffler.get(
				"Så... När var senaste gången du pratade med din mormor eller farmor?",
				"Vad sägs som att du städar ditt rum och frågar mig igen efter det?",
				"Jag slår vad om att du skulle älska att gå en runda. Du vet... utomhus?",
				"Jag vet om att du måste göra en massa andra saker. Vad sägs som att göra dem nu?",
				"Det ser ändå ut som att du behöver vila.",
				"Men kolla in denna super intressanta sidan på [https://sv.wikipedia.org/wiki/Special:Random wikipedia]!",
				"Låt oss kolla om det är någon bra som [http://www.twitch.tv/directory/game/Osu! streamar] just nu!",
				"Kolla, här är ett annat [http://dagobah.net/flash/Cursor_Invisible.swf spel] som du säkert suger på!",
                "Detta ger dig mycket tid att läsa igenom [https://github.com/Tillerino/Tillerinobot/wiki min manual].",
				"Oroa dig inte, dessa [https://www.reddit.com/r/osugame dank memes] borde fördriva tiden!",
				"Nu när du ändå är uttråkad, testa [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Rolig fråga: Om din hårddisk skulle krasha nu, hur mycket av din personliga data skulle du förlora?",
                "Så... Har du någonsin testat [https://www.google.se/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up utmaningen]?",
				"Du kan gå och göra något annat eller så kan vi stirra in i varandras ögon. I tystnad."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Jag har inte sett dig spela på ett tag.";
	}
	
	@Override
	public String isSetId() {
		return "Detta refererar en grupp med beatmaps, inte en enda beatmap.";
	}
}

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
 * @author BramboraSK majojo992@gmail.com https://github.com/BramboraSK https://osu.ppy.sh/u/BramboraSK
 */
public class Slovak extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
  static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Prepáč, ale mapa nie je k dispozícií. Možno je moc nová, ťažká, nehodnotená alebo nepatrí do osu standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Uhh... Vyzerá to tak, že ľudský Tillerino pokazil moju inštaláciu."
				+ " Pokiaľ si to čoskoro nevšimne, mohol/mohla by si [https://github.com/Tillerino/Tillerinobot/wiki/Contact ho upozorniť]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Čo sa to deje? Z osu! serveru pri prichádzajú nejaké kraviny..."
				+ " Ľudský Tillerino hovorí, že sa nejedná o nič, o čo by sme mali starať a mali by sme to skúsiť znovu."
				+ " Pokiaľ sa z nejakého dôvodu strašne obávaš, môžeš [https://github.com/Tillerino/Tillerinobot/wiki/Contact mu to povedať]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "žiadne dáta pre požadované módy";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Vitaj späť, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...si to ty? Tak dlho som ťa nevidel!"))
				.then(new Message("To je super, že si späť. Nechcel by si nejaké odporúčanie?"));
		} else {
			String[] messages = {
					"vyzeráš, že chceš nejaké doporučenie.",
					"rád ťa vidím! :)",
					"môj obľubený človek. (Nehovor to ostatným ľuďom!)",
					"aké príjemné prekvapenie! ^.^",
					"dúfal som, že sa ukážeš. Ostatní ľudia sú lamy, ale nehovor im to! :3",
					"ako sa máš?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "neznámý príkaz \"" + command
				+ "\". Napíš !help pokiaľ potrebuješ pomoc!";
	}

	@Override
	public String noInformationForMods() {
		return "Prepáč, momentálne ti nedokážem poskytnúť informácie o týchto módoch.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Tieto módy nie sú správne. Módy môžú byť rôzne kombinácie DT HR HD HT EZ NC FL SO NF. Skombinuj ich bez medzier a špeciálnych znakov. Napríklad: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nepamätám si, že by som ti dával informácie o nejakej pesničke...";
	}

	@Override
	public String tryWithMods() {
		return "Skús túto mapu s nejakými módmi!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Skús túto mapu s " + Mods.toShortNamesContinuous(mods);
	}
  
	@Override
	public String excuseForError() {
		return "Prepáč, ale bola tam taká pekná postupnosť jedničiek a núl. Ešte raz, prosím.";
	}

	@Override
	public String complaint() {
		return "Tvoja sťažnosť bola odoslaná. Tillerino sa na ňu pozrie čo najskôr.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Poď sem, ty!")
			.then(new Action("objíma " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Ahoj! Ja som robot, ktorý zabil Tillerina a prevzal jeho účet. Robím si srandu, ale veľmi často používam jeho účet."
				+ " [https://twitter.com/Tillerinobot status a aktualizácie]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki príkazy]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Často kladené otázky]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Je mi ľúto, ale v tejto chvíli " + feature + " je sprístupnená len pre hráčov, ktorí prekonali rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Čo myslíš tým, že si zmiešal bez módu s módmi?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Doporučil som ti všetko, čo mi prišlo na um]."
				+ " Skús ostatné možnosti doporučenia, alebo použi !reset. Pokiaľ si si neistý(á), pozri sa na !help.";
	}

	@Override
	public String notRanked() {
		return "Vyzerá to tak, že táto mapa nie je hodnotená.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Neplatná presnosť: \"" + acc + "\"";
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
		return new Message("[https://osu.ppy.sh/u/BramboraSK BramboraSK] mi pomohol naučiť sa slovensky.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Prepáč, ale \"" + invalid
				+ "\" sa nepočítá. Skús tieto: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Parameter nastavíš pomocou !set option (nastavenie) value (hodnota). Skús !help pokiaľ potrebuješ dalšie ukazovatele.";
	}
	
  StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
  
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "V tejto chvíli sú osu! servery super pomalé, takže momentálne ti nemôžem pomôcť. ";
		return message + apiTimeoutShuffler.get(
			        "No čo, používaš tablet, alebo myš? :3",
			        "Kedže tak rád tapuješ, aké switche používaš?",
				"Povedz... Kedy si naposledy hovoril(a) so svojou babičkou?",
				"Čo hovoríš na to, že si upraceš izbu a potom sa spýtaš znovu?",
				"Vsadím sa, že by si sa veľmi rád/rada išiel/išla prejsť. Ty vieš... von?",
				"Viem, že máš ešte veľa vecí, ktoré musíš urobiť. Necheš si ich teraz urobiť?",
				"Aj tak vyzeráš, že by si si mal(a) zdriemnuť.",
				"Ale pozri sa na túto zaujímavú stránku na [https://en.wikipedia.org/wiki/Special:Random wikipédií]!",
				"Poďme zistiť, či niekto dobrý momentálne [http://www.twitch.tv/directory/game/Osu! streamuje]!",
				"Pozri, tu je ďalšia [http://dagobah.net/flash/Cursor_Invisible.swf hra], v ktorej jsi pravdepodobne zlý(á)!",
				"Toto by ti malo dať dostatok času na preštudovanie [https://github.com/Tillerino/Tillerinobot/wiki môjho návodu].",
				"Neboj sa, tieto [https://www.reddit.com/r/osugame dank memes] by ti mali pomôcť zabiť nejaký ten čas.",
				"Zatiaľ čo sa nudíš, vyskúšaj [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Zábavná otázka: Pokiaľ by sa ti teraz pokazil harddrive, koľko tvojich osobných údajov môže byť navždy stratených?",
				"Takže... Už si niekdy skúsil(a) [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge Bring Sally Up - klikovaciu výzvu]?",
				"Môžeš ísť robiť niečo iné, alebo si môžeme navzájom civieť do očí. Potichu."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "V poslednej dobe som ťa nevidel hrať.";
	}
	
	@Override
	public String isSetId() {
		return "Toto odkazuje na balíčok pesničiek, nie na určitú pesničku.";
	}
}

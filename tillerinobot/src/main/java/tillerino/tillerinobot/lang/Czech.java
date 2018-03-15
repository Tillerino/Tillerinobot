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
 * @author Polarni tom.rivnac@gmail.com https://github.com/Polarni https://osu.ppy.sh/u/Polarni
 */
public class Czech extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
  static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Omlouvám se, ale beatmapa není k dispozici. Možná je moc nová, těžká, nehodnocená nebo nepatří do osu! standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Uff... Vypadá to že lidský Tillerino zvoral mojí instalaci."
				+ " Pokud si toho brzy nevšimne, mohl(a) by jsi [https://github.com/Tillerino/Tillerinobot/wiki/Contact ho upozornit]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Co se děje? Dostávám nesmyly z osu! serveru. Můžeš mi říct, co to znamená? 0011101001010000"
				+ " Lidský Tillerino říká že se nejedná o nic o co by jsme se museli starat a měli bychom to zkusit znovu."
				+ " Pokud se z nějakého důvodu obáváš, můžeš [https://github.com/Tillerino/Tillerinobot/wiki/Contact mu to říct]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "žádné data pro požadované mody";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Vítej zpět, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...jsi to ty? Už je to nějaká doba!"))
				.then(new Message("To je dobře, že jsi zpátky. Mohu tě zaujmout doporučením?"));
		} else {
			String[] messages = {
					"vypadáš jako že chceš doporučení.",
					"rád tě vidím! :)",
					"můj oblíbený člověk. (Neříkej to ostatním lidem!)",
					"to je příjemné překvapení! ^.^",
					"Doufal jsem že se ukážeš. Ostatní lidi jsou lamy, ale neříkej jim to! :3",
					"Jak se máš?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "neznámý příkaz \"" + command
				+ "\". Zadej !help pokud potřebuješ pomoc!";
	}

	@Override
	public String noInformationForMods() {
		return "Omlouvám se, ale v tuto chvíli nemohu poskytnout informace pro tyto mody.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Tyto mody nevypadají dobře. Mody můžou být různé kombinace DT HR HD HT EZ NC FL SO NF. Zkombinuj je bez mezer a speciálních znaků. Například: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nepamatuji si že můžeš dostat informace o jakékoliv písničce...";
	}

	@Override
	public String tryWithMods() {
		return "Zkus tuto mapu s některými mody!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Zkus tuto mapu s " + Mods.toShortNamesContinuous(mods);
	}
  
	@Override
	public String excuseForError() {
		return "Omlouvám se, byla tam krásná posloupnost jedniček a nul a nechal jsem se rozptýlit. Ješte jednou prosím.";
	}

	@Override
	public String complaint() {
		return "Tvoje stížnost byla podána. Tillerino se na ní podívá hned jak bude moct.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Pojď sem, ty!")
			.then(new Action("objetí " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Ahoj! Já jsem robot který zabil Tillerino a převzal jeho účet. Dělám si srandu, ale hodně používám jeho účet."
				+ " [https://twitter.com/Tillerinobot status a aktualizace]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki příkazy]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Často kladené otázky]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Je mi líto, v tuto chvíli " + feature + " je jen přístup pro hráče, kteří překonali rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Co myslíš tím bez modu s modama?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Doporučil jsem vše, co mě napadlo]."
				+ " Zkus ostatní možnosti doporučení nebo použij !reset. Pokud si nejsi jistý(á) koukni na !help.";
	}

	@Override
	public String notRanked() {
		return "Vypadá to že beatmapa není hodnocená.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Neplatná přesnost: \"" + acc + "\"";
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
		return new Message("[https://osu.ppy.sh/u/Polarni Polarni] mi pomohl naučit se česky.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Omlouvám se, ale \"" + invalid
				+ "\" se nepočítá. Zkus tyto: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Syntaxe pro nastavení parametru je !set option (nastavení) value (hodnota). Zkus !help pokud potřebuješ další ukazatele.";
	}
	
  StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
  
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "V tuto chvíli jsou osu! servery super pomalé, takže pro tuto chvíli nemůžu nic pro tebe udělat. ";
		return message + apiTimeoutShuffler.get(
				"Řekni... Kdy jsi naposledy mluvil(a) se svojí babičkou?",
				"Co říkáš na úklid tvého pokoje a pak se zkusit zeptat znova?",
				"Vsadím se že by jsi se moc rád(a) běžel(a) projít. Ty víš... venku?",
				"Já jen vím že máš spoust jiných věcí na dělání. Co třeba jít je udělat?",
				"Stejně vypadáš jako kdyby jsi si potřeboval(a) zdřímnout.",
				"Ale podívej se na tuhle zajímavou stránku na [https://en.wikipedia.org/wiki/Special:Random wikipedii]!",
				"Pojďme zjistit jestli je někdo dobrý ve [http://www.twitch.tv/directory/game/Osu! streamování]!",
				"Podívej, tady je jiná [http://dagobah.net/flash/Cursor_Invisible.swf hra] ve který jsi pravděpodobně špatný(á)!",
				"Tohle by ti mělo dát dostatek času na prostudování [https://github.com/Tillerino/Tillerinobot/wiki mého návodu].",
				"Neboj se, tady jsou [https://www.reddit.com/r/osugame dank meme] které by měly zabít nějaký tvůj čas.",
				"Zatím co se nudíš, vyzkoušej [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Zábavná otázka: Pokud se teď tvůj pevný disk rozbije, kolik tvých osobních údajů může být navždy ztraceno?",
				"Takže... Už jsi někdy zkoušel(a) [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge Bring Sally Up - klikovací výzvu]?",
				"Můžeš jít dělat něco jinýho nebo můžem jít civět ostatním do očí. Tiše."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Neviděl jsem tě v poslední době hrát.";
	}
	
	@Override
	public String isSetId() {
		return "Tohle odkazuje na balíček beatmap a ne na určitou beatmapu.";
	}
}

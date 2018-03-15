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
 * @author Anze Jensterle hello@craftbyte.net https://github.com/CraftByte https://osu.ppy.sh/u/CraftByte
 */
public class Slovenian extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Žal te mape ne poznam. Lahko, da je zelo nova, zelo težka, neocenjena ali pa ni navaden osu način.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Izgleda da je človeški Tillerino nekaj zafrknil."
				+ " Če tega kmalu ne opazi, mu prosim [https://github.com/Tillerino/Tillerinobot/wiki/Contact sporoči]? (referenca "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Kaj se dogaja? Iz osu strežnika dobivam čudne signale. Ali veš kaj pomeni 0011101001010000?"
				+ " Človek Tillerino pravi da ni treba skrbeti, in da naj poskusiva znova."
				+ " Če te zaradi česa res skrbi, mu lahko to [https://github.com/Tillerino/Tillerinobot/wiki/Contact sporočiš]. (referenca "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "ni podatkov za izbrane mode";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Dobrodošel nazaj, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...si to ti? Kako dolgo se že nisva videla!"))
				.then(new Message("Lepo te je spet videti. Ali vam lahko ponudim kakšno priporočilo?"));
		} else {
			String[] messages = {
					"izgleda, kot da želiš priporočilo.",
					"kako lepo te je videti! :)",
					"moj najljubši človek. (Ne povej ostalim ljudem!)",
					"kakšno lepo presemečenje! ^.^",
					"čakal sem, da se boš prikazal. Vsi ostali ljudje so dolgočasni, ampak ne povej jim, da sem to rekel! :3",
					"kaj boš kaj počel danes?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Neznan ukaz \"" + command
				+ "\". Napiši !help če potrebuješ pomoč!";
	}

	@Override
	public String noInformationForMods() {
		return "Žal glede izbranih modov trenutno nimam informacij.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Te modi ne izgledajo pravilno. Modi so lahko katera koli kombinacija DT HR HD HT EZ NC FL SO NF. Skombiniraj jih brez presledkov in drugih znakov. Primer: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Ne spomnim se, da bi ti dal kakšno pesem...";
	}

	@Override
	public String tryWithMods() {
		return "Poskusi to mapo z modi!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Poskusi to mapo z " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Žal mi je, bidel sem tako lepo kombinacijo enk in ničel in pozabil o čem sva govorila. Kaj si želel?";
	}

	@Override
	public String complaint() {
		return "Tvoja pritožba je bila sprejeta. Tillerino jo bo obdelal, ko jo bo lahko.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Pridi sem!")
			.then(new Action("objame " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hej! Sem robot, ki je ubil Tillerina in prevzel njegov račun. Hec, ampak račun veliko uporabljam."
				+ " [https://twitter.com/Tillerinobot status in novice]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki ukazi]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Pogosta vprašanja]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Žal je trenutno " + feature + " na voljo je igralcem nad nivojem " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Kako misliš nomod z modi?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Priporočil sem vse kar mi je padlo na pamet]."
				+ " Poskusi druge načine priporočila ali pa !reset. Če nisi prepričan, poiskusi !help.";
	}

	@Override
	public String notRanked() {
		return "Izgleda, da ta mapa ni ocenjena.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Neveljavna natančnost: \"" + acc + "\"";
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
		return new Message("V sloveščino me je prevedel [https://osu.ppy.sh/u/CraftByte CraftByte]");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Žal mi je, ampak \"" + invalid
				+ "\" ni veljaven vnos. Poskusi to: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Sintaksa za nastavitev parametra je !set nastavitev vrednost. Poskusi !help za več informacij.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "osu! strežniki so trenutno super počasni, zato trenutno ne morem narediti nič. ";
		return message + apiTimeoutShuffler.get(
				"torej... Kdaj si nazadnje poklical babico?",
				"Kaj pa če pospraviš sobo in potem poskusiva še enkrat?",
				"Stavim da bi ti bil všeč en sprehod. Saj veš... zunaj?",
				"Vem da imaš kar nekaj stvari za postoriti. Kaj pa če bi jih naredil zdaj?",
				"Tako ali tako izgledaš kot da rabiš počitek.",
				"Ampak poglej to zelo zanimivo stran na [https://sl.wikipedia.org/wiki/Special:Random Wikipedii]!",
				"Poglejva, če kdo dober [http://www.twitch.tv/directory/game/Osu! streama] osu!",
				"Glej, še ena [http://dagobah.net/flash/Cursor_Invisible.swf igra] v kateri si verjetno slab!",
				"To bo zagotovo dovolj časa, da prebereš [https://github.com/Tillerino/Tillerinobot/wiki moja navodila].",
				"Ne skrbi, [https://www.reddit.com/r/osugame hecne meme] bodo naredile, da čas teče hitreje.",
				"Ko ti je že dolgčas, poskusi [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Vprašanje: IČe bi se tvoj trdi disk ravnokar uničil, koliko tvojih podatkov bi izgubil za vedno?",
				"Torej... Ali si že poiskusil [https://www.google.si/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"Lahko greš delat kaj drugega, lahko pa se gledava iz oči v oči. Čisto tiho."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Zadnje čase te nisem videl igrati.";
	}
	
	@Override
	public String isSetId() {
		return "To je zbirka map, ne ena mapa.";
	}
}

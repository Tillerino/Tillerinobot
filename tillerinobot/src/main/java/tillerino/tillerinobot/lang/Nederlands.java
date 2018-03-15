package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * Dutch language implementation by https://osu.ppy.sh/u/PudiPudi and https://github.com/notadecent and https://osu.ppy.sh/u/2756335
 */
public class Nederlands extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Het spijt me, ik ken die map niet. Hij kan gloednieuw zijn, heel erg moeilijk of hij is niet voor osu standard mode.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Lijkt er op dat Tillerino een oelewapper is geweest en mijn bedrading kapot heeft gemaakt."
				+ " Als hij het zelf niet merkt, kan je hem dan [https://github.com/Tillerino/Tillerinobot/wiki/Contact op de hoogte stellen]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Wat gebeurt er? Ik krijg alleen maar onzin van de osu server. Kan je me vertellen wat dit betekent? 00111010 01011110 00101001"
				+ " De menselijke Tillerino zegt dat we ons er geen zorgen over hoeven te maken en dat we het opnieuw moeten proberen."
				+ " Als je je heel erg zorgen maakt hierover, kan je het aan Tillerino [https://github.com/Tillerino/Tillerinobot/wiki/Contact vertellen]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Geen informatie beschikbaar voor opgevraagde mods";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Welkom terug, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...ben jij dat? Dat is lang geleden!"))
				.then(new Message("Het is goed om je weer te zien. Kan ik je wellicht een recommandatie geven?"));
		} else {
			String[] messages = {
					"jij ziet er uit alsof je een recommandatie wilt.",
					"leuk om je te zien! :)",
					"mijn favoriete mens. (Vertel het niet aan de andere mensen!)",
					"wat een leuke verrassing! ^.^",
					"Ik hoopte al dat je op kwam dagen. Al die andere mensen zijn saai, maar vertel ze niet dat ik dat zei! :3",
					"waar heb je zin in vandaag?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Ik snap niet helemaal wat je bedoelt met \"" + command
				+ "\". Typ !help als je hulp nodig hebt!";
	}

	@Override
	public String noInformationForMods() {
		return "Sorry, ik kan je op het moment geen informatie geven over die mods.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Die mods zien er niet goed uit. Mods kunnen elke combinatie zijn van DT HR HD HT EZ NC FL SO NF. Combineer ze zonder spaties of speciale tekens, bijvoorbeeld: '!with HDHR' of '!with DTEZ'";
	}

	@Override
	public String noLastSongInfo() {
		return "Ik kan me niet herinneren dat je al een map had opgevraagd...";
	}

	@Override
	public String tryWithMods() {
		return "Probeer deze map met wat mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Probeer deze map eens met " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Het spijt me, een prachtige rij van enen en nullen kwam langs en dat leidde me af. Wat wou je ook al weer?";
	}

	@Override
	public String complaint() {
		return "Je klacht is ingediend. Tillerino zal er naar kijken als hij tijd heeft.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Kom eens hier jij!")
			.then(new Action("knuffelt " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hallo! Ik ben de robot die Tillerino heeft gedood en zijn account heeft overgenomen! Grapje, maar ik gebruik wel zijn account."
				+ " Check https://twitter.com/Tillerinobot voor status en updates!"
				+ " Zie https://github.com/Tillerino/Tillerinobot/wiki voor commandos!";
	}

	@Override
	public String faq() {
		return "Zie https://github.com/Tillerino/Tillerinobot/wiki/FAQ voor veelgestelde vragen!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, " + feature + " is op het moment alleen beschikbaar voor spelers boven rank " + minRank ;
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Hoe bedoel je, nomod met mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Ik heb je alles wat ik me kan bedenken al aanbevolen]."
				+ " Probeer andere aanbevelingsopties of gebruik !reset. Als je het niet zeker weet, check !help.";
	}

	@Override
	public String notRanked() {
		return "Lijkt erop dat die beatmap niet ranked is.";
	}
	
	@Override
	public String invalidAccuracy(String acc) {
		return "Ongeldige accuracy: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("PudiPudi heeft me geleerd Nederlands te spreken.");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Het spijt me, maar \"" + invalid
				+ "\" werkt niet. Probeer deze: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "De syntax om een parameter in te stellen is '!set optie waarde'. Typ !help als je meer aanwijzingen nodig hebt.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);

	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "De osu! servers zijn nu heel erg traag, dus ik kan op dit moment niets voor je doen. ";
		return message + apiTimeoutShuffler.get(
				"Zeg... Wanneer heb je voor het laatst met je oma gesproken?",
				"Wat dacht je ervan om je kamer eens op te ruimen en dan nog eens te proberen?",
				"Ik weet zeker dat je vast erg zin hebt in een wandeling. Jeweetwel... daarbuiten?",
				"Ik weet gewoon zeker dat er een helehoop andere dingen zijn die je nog moet doen. Wat dacht je ervan om ze nu te doen?",
				"Je ziet eruit alsof je wel wat slaap kan gebruiken...",
				"Maat moet je deze superinteressante pagina op [https://nl.wikipedia.org/wiki/Special:Random wikipedia] eens zien!",
				"Laten we eens kijken of er een goed iemand aan het [http://www.twitch.tv/directory/game/Osu! streamen] is!",
				"Kijk, hier is een ander [http://dagobah.net/flash/Cursor_Invisible.swf spel] waar je waarschijnlijk superslecht in bent!",
				"Dit moet je tijd zat geven om [https://github.com/Tillerino/Tillerinobot/wiki mijn handleiding] te bestuderen.",
				"Geen zorgen, met deze [https://www.reddit.com/r/osugame dank memes] kun je de tijd dooden.",
				"Terwijl je je verveelt, probeer [http://gabrielecirulli.github.io/2048/ 2048] eens een keer!",
				"Leuke vraag: Als je harde schijf op dit moment zou crashen, hoeveel van je persoonlijke gegevens ben je dan voor eeuwig kwijt?",
				"Dus... Heb je wel eens de [https://www.google.nl/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge] geprobeerd?",
				"Je kunt wat anders gaan doen of we kunnen elkaar in de ogen gaan staren. In stilte."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Ik heb je de afgelopen tijd niet zien spelen.";
	}
	
	@Override
	public String isSetId() {
		return "Dit refereerd naar een beatmap-verzameling in plaats van een beatmap zelf.";
	}
}

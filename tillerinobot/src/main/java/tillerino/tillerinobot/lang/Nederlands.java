package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * Dutch language implementation by https://osu.ppy.sh/u/PudiPudi and https://github.com/notadecent
 */
public class Nederlands implements Language {

	@Override
	public String unknownBeatmap() {
		return "Het spijt me, ik ken die map niet. Hij kan gloednieuw zijn, heel erg moeilijk of hij is niet voor osu standard mode.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Lijkt er op dat Tillerino een oelewapper is geweest en mijn bedrading kapot heeft gemaakt."
				+ " Als hij het zelf niet merkt, kan je hem er dan een berichtje over sturen? @Tillerino of /u/Tillerino? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Wat gebeurt er? Ik krijg alleen maar onzin van de osu server. Kan je me vertellen wat dit betekent? 00111010 01011110 00101001"
				+ " De menselijke Tillerino zegt dat we ons er geen zorgen over hoeven te maken en dat we het opnieuw moeten proberen."
				+ " Als je je heel erg zorgen maakt hierover, kan je het aan Tillerino vertellen @Tillerino of /u/Tillerino. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Geen informatie beschikbaar voor opgevraagde mods";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Welkom terug, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...ben jij dat? Dat is lang geleden!");
			user.message("Het is goed om je weer te zien. Kan ik je wellicht een recommandatie geven?");
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
			
			user.message(apiUser.getUserName() + ", " + message);
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
		return "Je naam verwart me. Ben je geband? Zoniet, neem contact op met @Tillerino of /u/Tillerino (reference "
				+ exceptionMarker + ")";
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
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Kom eens hier jij!");
		user.action("knuffelt " + apiUser.getUserName());
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
		return "Ik heb je alles wat ik me kan bedenken al aanbevolen."
				+ " Probeer andere aanbevelingsopties of gebruik !reset. Als je het niet zeker weet, check !help.";
	}

	@Override
	public String notRanked() {
		return "Lijkt erop dat die beatmap niet ranked is.";
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
		return "Ongeldige accuracy: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("PudiPudi heeft me geleerd Nederlands te spreken.");
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
}

package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

public class Default implements Language {

	@Override
	public String unknownBeatmap() {
		return "Beklager, jeg kjenner ikke til det kartet. Det kan hende den er ny, veldig vanskelig, ikke rangert eller ikke standard osu mode.";
	}

	@Override
	public String internalException(String marker) {
		return "Æsj... Ser ut som menneske Tillerino har gjort en feil i koblingen."
				+ " Om han ikke legger merke til det, kunne du ha [https://github.com/Tillerino/Tillerinobot/wiki/Contact informert han]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Hva skjer? Jeg får bare tull og tøys fra osu serveren. Kan du fortelle meg hva dette skal bety? 0011101001010000"
				+ " Menneske Tillerino sier at dette ikke er noe å bekymre seg for og at vi burde prøve igjen."
				+ " Om du er veldig bekymret for en eller annen grunn, kan du [https://github.com/Tillerino/Tillerinobot/wiki/Contact fortelle han om det. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Ingen data angående forespurte modifikasjoner.";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("Bip Bop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Velkommen tilbake, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...Er det deg? Det var lenge siden!");
			user.message("Det er godt å ha deg tilbake. Kan jeg komme med en anbefaling?");
		} else {
			String[] messages = {
					"Du ser ut som du vil ha en anbefaling.",
					"Så fint å se deg! :)",
					"Mitt favoritt menneske. (Ikke fortell de andre menneskene!)",
					"For en deilig overaskelse! ^.^",
					"Jeg håpet på at du ville vise deg. Alle de andre menneskene suger, men ikke fortell dem at jeg sa det! :3",
					"Hva føler du for å gjøre idag?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command \"" + command
				+ "\". Skriv !help om du trenger hjelp!";
	}

	@Override
	public String noInformationForMods() {
		return "Beklager, jeg kan ikke tilby informasjon for disse modifikasjonene akkurat nå.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Disse modifikasjonene ser ikke rette ut. Modifikasjoner kan være hvilken som helst kombinasjon av  DT HR HD HT EZ NC FL SO NF. Kombiner dem uten noe mellomrom eller spesielle bokstaver. Eksempel: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Jeg kan ikke huske at du har fått noe sang informasjon...";
	}

	@Override
	public String tryWithMods() {
		return "Prøv dette kartet med noen modifikasjoner!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prøv dette kartet med " + Mods.toShortNamesContinuous(mods);
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
		return "Navnet ditt er forvirrende. Er du utestengt? Hvis ikke, vær så snill og [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Jeg beklager, det var en vakker sekvens av ener og nuller og jeg ble helt distrahert. Hva vart det du ville igjen?";
	}

	@Override
	public String complaint() {
		return "Din klage har blitt registrert. Tillerino vil se på det her når han kan.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Kom her, du!");
		user.action("klemmer " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Hei! Jeg er roboten som drepte Tillerino og tok over brukeren hans. Kødder bare, men jeg bruker den ganske mye."
				+ " [https://twitter.com/Tillerinobot status og oppdateringer]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki kommandoer]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Ofte stilte spørsmål]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Beklager, akkurat nå så er " + feature + " kun tilgjengelig for spillere som har passert rank" + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Hva mener du med nomod med modifikasjoner?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Jeg har anbefalt alt jeg kan tenke på."
				+ " Prøv andre anbefalings alternativer eller bruk !reset. Om du er usikker, sjekk ut !help.";
	}

	@Override
	public String notRanked() {
		return "Ser ut som det kartet der er urangert.";
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
		return "Ugyldig presisjon: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Så du liker meg for den jeg er :))))");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Jeg beklager, men \"" + invalid
				+ "\" beregnes ikke. Prøv disse: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "The syntax to set a parameter is !set alternativ verdi. Prøv !help om du behøver flere veiledninger.";
	}
}

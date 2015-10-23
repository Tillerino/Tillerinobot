package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * Polish language implementation by https://osu.ppy.sh/u/pawwit
 */
public class Polski implements Language {

	@Override
	public String unknownBeatmap() {
		return "Przykro mi, nie znam tej mapy. Możliwe że jest ona nowa albo nierankingowa.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Wygląda na to że Tillerino (człowiek) uszkodził moje obwody."
				+ "Gdyby wkrótce tego nie zauważył, Możesz go poinformować? Znajdziesz go na @Tillerino lub /u/Tillerino? (odwołanie "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Co jest?! Odpowiedź serwera osu nie ma sensu. Możesz mi powiedzieć co to znaczy \"0011101001010000\"?"
				+ " Tillerino (człowiek) mówi, żeby się tym nie przejmować oraz że powinniśmy spróbować jeszcze raz."
				+ " Jeśli jesteś bardzo zaniepokojony z jakiegoś powodu, możesz mu powiedzieć o tym na @Tillerino lub /u/Tillerino. (odwołanie "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "brak danych dla wskazanych modów";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Witaj ponownie, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...czy to Ty? Minęło sporo czasu!");
			user.message("Dobrze znowu Cie widzieć. Chcesz usłyszeć kilka rekomendacji?");
		} else {
			String[] messages = {
					"wygląda na to że chcesz jakieś rekomendacje.",
					"jak dobrze Cie widzieć! :)",
					"mój ulubiony człowiek. (Nie mów o tym innym ludziom!)",
					"jakie miłe zaskoczenie! ^.^",
					"Miałem nadzieję, że się pojawisz. Jesteś fajniejszy niż inni ludzie :3 (nie mówi im że Ci to powiedziałem!)",
					"jak się masz?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "nieznana komenda \"" + command
				+ "\". jeśli potrzebujesz pomocy napisz \"!help\" !";
	}

	@Override
	public String noInformationForMods() {
		return "Przykro mi, nie mogę dostarczyć informacji dla tych modów w tym momencie.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Coś się nie zgadza. Mody mogą być dowolną kombinacją DT HR HD HT EZ NC FL SO NF. Łącz je nie używając spacji, ani żadnych znaków. Przykład: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nie pamiętam żebyś pytał się ostatnio o jakąś piosenkę...";
	}

	@Override
	public String tryWithMods() {
		return "Spróbuj zagrać tą mapę z modami!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Spróbuj zagrać tą mapę z " + Mods.toShortNamesContinuous(mods);
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
		return "Twoja nazwa wydaje mi się jakaś dziwna. Jesteś zbanowany? Jeśli nie napisz na @Tillerino lub /u/Tillerino (odwołanie "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Wybacz, widziałem piękną sekwencję zer i jedynek przez co się trochę rozkojarzyłem. Mógłbyś powtórzyć co chciałeś?";
	}

	@Override
	public String complaint() {
		return "Twoje zgłoszenie zostało wypełnione. Tillerino zerknie na nie jak tylko będzie mógł.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Chodź tu!");
		user.action("przytula " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Hej! Jestem robotem który zabił Tillerino, aby przejąc jego konto. Tylko żartowałem, czasem używam tego konta."
				+ " Sprawdź https://twitter.com/Tillerinobot żeby zobaczyć najnowsze aktualizacje!"
				+ " Odwiedź https://github.com/Tillerino/Tillerinobot/wiki żeby poznać komendy!";
	}

	@Override
	public String faq() {
		return "Wejdź na https://github.com/Tillerino/Tillerinobot/wiki/FAQ aby zobaczyć FAQ!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Przepraszam, ale w tym momencie " + feature + " jest dostępna tylko dla ludzi którzy przekroczyli pozycję " + minRank + " w rankingu.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Jak chcesz połączyć brak modów z modami?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Skończyły mi się pomysł co mogę Ci jeszcze polecić."
				+ " Sprawdź inne opcje polecania albo użyj komendy !reset. Jeśli nie wiesz o co mi chodzi wpisz !help.";
	}

	@Override
	public String notRanked() {
		return "Wygląda na to, że ta mapa nie jest rankingowa.";
	}

	@Override
	public void optionalCommentOnNP(IRCBotUser user,
			OsuApiUser apiUser, BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
		// return "Ok, zapamiętam tą mapę!";
	}

	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser,
			BeatmapMeta meta) {
		// regular Tillerino doesn't comment on this
		// return "Ok, zapamiętam te mody";
	}

	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user,
			OsuApiUser apiUser, Recommendation meta) {
		// regular Tillerino doesn't comment on this
		// I have no idea what Tillerino can say with recommendation
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
		return "Nieprawidłowa celność: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Pawwit nauczył mnie jak mówić po polsku, jeśli uważasz, że gdzieś się pomylił napisz do niego na osu!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Wybacz, nie wiem co \"" + invalid
				+ "\" znaczy. Spróbuj: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Składnia polecenia \"!set\" jest następująca \"!set opcja wartość\". Wpisz !help jeśli potrzebujesz więcej wskazówek.";
	}
	
	@Override
	public String apiTimeoutException() {
		return new Default().apiTimeoutException();
	}
	
	@Override
	public String noRecentPlays() {
		return new Default().noRecentPlays();
	}
	
	@Override
	public String isSetId() {
		return new Default().isSetId();
	}
	
	@Override
	public String getPatience() {
		return new Default().getPatience();
	}
}
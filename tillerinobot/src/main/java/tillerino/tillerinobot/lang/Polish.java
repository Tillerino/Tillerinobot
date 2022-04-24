package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Action;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

/**
 * Polish language implementation.
 * Pawwit https://osu.ppy.sh/u/2070907 & LilSilv https://github.com/LilSilv https://osu.ppy.sh/users/8488688
 */
public class Polish extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;

	@Override
	public String unknownBeatmap() {
		return "Przykro mi, nie rozpoznaję tej mapy. Możliwe że jest nowa, bardzo trudna, nierankingowa lub z innego trybu niż osu!standard";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Wygląda na to, że ludzki Tillerino uszkodził moje obwody."
				+ " Gdyby wkrótce tego nie zauważył, mógłbyś go [https://github.com/Tillerino/Tillerinobot/wiki/Contact poinformować]? (odwołanie "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Co jest?! Odpowiedź serwera osu nie ma sensu. Możesz mi powiedzieć, co to ma znaczyć? 0011101001010000"
				+ " Ludzki Tillerino mówi, żeby się tym nie przejmować, i że powinniśmy spróbować jeszcze raz."
				+ " Jeżeli z jakiegoś powodu jesteś zaniepokojony, możesz [https://github.com/Tillerino/Tillerinobot/wiki/Contact powiedzieć mu] o tym. (odwołanie "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "brak danych dla wskazanych modów";
	}

	@Override
	public GameChatResponse welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Witaj ponownie, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...czy to Ty? Minęło sporo czasu!"))
				.then(new Message("Dobrze znowu Cię widzieć. Chcesz usłyszeć kilka rekomendacji?"));
		} else {
			String[] messages = {
					"wyglądasz jakbyś chciał rekomendacji.",
					"jak dobrze Cię widzieć! :)",
					"mój ulubiony człowiek. (Nie mów o tym innym człowiekom!)",
					"jakie miłe zaskoczenie! ^.^",
					"Miałem nadzieję, że się pojawisz. Jesteś fajniejszy niż inni ludzie, ale nie mów im, że Ci to powiedziałem! :3",
					"na co masz dzisiaj ochotę?",
			};

			String message = messages[ThreadLocalRandom.current().nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Nieznana komenda \"" + command
				+ "\". Napisz !help jeśli potrzebujesz pomocy!";
	}

	@Override
	public String noInformationForMods() {
		return "Przykro mi, nie mogę dostarczyć informacji dla tych modów w tym momencie.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Coś się nie zgadza. Mody mogą być dowolną kombinacją DT HR HD HT EZ NC FL SO NF. Łącz je nie używając spacji ani żadnych innych znaków. Przykład: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Nie pamiętam, żebyś pytał się ostatnio o jakąś mapę...";
	}

	@Override
	public String tryWithMods() {
		return "Spróbuj zagrać tę mapę z modami!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Spróbuj zagrać tę mapę z " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Wybacz, widziałem piękną sekwencję zer i jedynek, przez co trochę się rozkojarzyłem. Mógłbyś powtórzyć co chciałeś?";
	}

	@Override
	public String complaint() {
		return "Twoje zgłoszenie zostało wypełnione. Tillerino zerknie na nie, jak tylko będzie mógł.";
	}

	@Override
	public GameChatResponse hug(OsuApiUser apiUser) {
		return new Message("Chodź no tu!")
			.then(new Action("przytula " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hej! Jestem robotem, który zabił Tillerino i przejął jego konto. Żartowałem, ale często używam tego konta."
				+ "  [https://twitter.com/Tillerinobot status i aktualizacje]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki komendy]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Często zadawane pytania]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Wybacz, ale w tym momencie " + feature + " jest dostępna tylko dla graczy, którzy przekroczyli pozycję " + minRank + " w rankingu.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Jak chcesz połączyć brak modów z modami?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Skończyły mi się pomysły co mogę Ci jeszcze polecić]."
				+ " Sprawdź inne opcje polecania albo użyj komendy !reset. Jeśli potrzebujesz więcej szczegółów, wpisz !help.";
	}

	@Override
	public String notRanked() {
		return "Wygląda na to, że ta mapa nie jest rankingowa.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Nieprawidłowa celność: \"" + acc + "\"";
	}

	@Override
	public GameChatResponse optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("[https://osu.ppy.sh/users/1698537 Pawwit] i [https://osu.ppy.sh/users/8488688 Lil Silv] nauczyli mnie mówić po polsku. Jeśli uważasz, że gdzieś się pomylili, napisz do nich na osu!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Wybacz, nie wiem co oznacza \"" + invalid
				+ "\". Spróbuj: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Składnia polecenia !set jest następująca: !set opcja wartość. Wpisz !help jeśli potrzebujesz więcej wskazówek.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(ThreadLocalRandom.current());
	
	@Override
	public String apiTimeoutException() {
		registerModification(); 
		final String message = "Serwery osu! obecnie działają bardzo wolno, więc w tym momencie nie mogę Tobie pomóc. ";
		return message + apiTimeoutShuffler.get(
                "Powiedz... Kiedy był ostatni raz, gdy rozmawiałeś ze swoją babcią?",
                "Może posprzątasz swój pokój, a potem zapytasz jeszcze raz?",
                "Stawiam, że chętnie byś poszedł na spacerek. Wiesz... na zewnątrz",
                "Jestem pewien, że masz kilka innych rzeczy do zrobienia. Może zrobisz je teraz?",
                "Wyglądasz jakbyś potrzebował drzemki",
                "Ale sprawdź tą super interesującą stronę na [https://pl.wikipedia.org/wiki/Special:Random Wikipedii]!",
                "Sprawdźmy czy ktoś niezły teraz [http://www.twitch.tv/directory/game/Osu! streamuje]!",
                "Zobacz, kolejna [http://dagobah.net/flash/Cursor_Invisible.swf gra], w którą pewnie ssiesz!",
                "Powinieneś mieć teraz wystarczająco dużo czasu na przeczytanie [https://github.com/Tillerino/Tillerinobot/wiki mojej instrukcji].",
                "Nie martw się, te [https://www.reddit.com/r/osugame dank memy] powinny Ci pomóc zabić czas.",
                "Jeśli się nudzisz, wypróbuj [http://gabrielecirulli.github.io/2048/ 2048]!",
                "Takie tam pytanie: Jeśli twój dysk twardy by się teraz zepsuł, ile twoich osobistych danych przepadłoby na zawsze?",
                "Więc... Próbowałeś kiedyś [https://www.google.pl/search?q=bring%20sally%20up%20push%20up%20challenge wyzwania sally up push up]?",
                "Możesz iść robić coś innego, lub możemy gapić się na siebie nawzajem. W ciszy."
				);
	}

	@Override
	public String noRecentPlays() {
		return "Nie widziałem, żebyś ostatnio grał.";
	}
	
	@Override
	public String isSetId() {
		return "To odwołuje się do zestawu map, a nie do jednej mapy.";
	}
}

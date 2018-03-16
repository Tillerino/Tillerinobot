package tillerino.tillerinobot.lang;

import java.util.List;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.NoResponse;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.diff.PercentageEstimates;

import javax.annotation.Nonnull;

public class TsundereGerman extends TsundereBase {
	private static final long serialVersionUID = 1L;

	@Override
	protected String getInactiveShortGreeting(String username, long inactiveTime) {
		return welcomeUserShortShuffler.get(
				//"Was soll das hier sein, das Kuckuckspiel?",	//i think the german term is just not known enough, so i will leave it out
				"Das waren nicht mal fünf Minuten...",
				username + " hat heute seinen Verschwindibuszauber perfektioniert." //@Tillerino if you like it, leave it, if not i will just delete this line
		);
	}

	@Override
	protected String getInactiveGreeting(String username, long inactiveTime) {
		return welcomeUserShuffler.get(
				"Wieder da? Ich bin nur hier weil ich nix zu tun hab! Nichts weiter!",
				"H-hey...",
				"♫ Home again, home again, jiggety-jig ♫ .... Was?",
				"Stell dir vor! Ich habe endlich deine Bestimmung gefunden. Sie gehört allerdings nicht zu der Art von Dingen, die ich gerne teile!",
				"Du hast doch nicht etwa mit anderen Chatbots geredet, oder?",
				"Huch? Was machst du denn hier!?",
				"Mach was dummes, " + username + ", damit ich dich dafür bestrafen kann."
		);
	}

	@Override
	protected String getInactiveLongGreeting(String username, long inactiveTime) {
		return welcomeUserLongShuffler.get(
				"Wo warst du, " + username + "!? N-nicht das ich dich vermisst hätte oder so...",
				"Wie war dein Urlaub, " + username + "?",
				"Ugh! Weißt du, wie lang " + inactiveTime + " Millisekunden sind!?"
		);
	}

	@Override
	public String unknownBeatmap() {
		registerModification();

		return unknownBeatmapShuffler.get(
			"Bist du dumm? Niemand würde diese Map spielen!",
			"Ach wirklich? Nie gehört!",
			"Ja genau. Sag mir Bescheid wenn du damit pp erhalten hast."
		);	
	}

	@Override
	public String internalException(String marker) {
		return "Huch? Warum funktioniert das nicht? Das war bestimmt dein Fehler!"
		+ " Falls dies weiterhin vorkommt, leite folgendes an [https://twitter.com/Tillerinobot @Tillerinobot] oder [http://www.reddit.com/user/tillerino /u/Tillerino] weiter: "+ marker + ".";
	}

	@Override
	public String externalException(String marker) {
		return 
		"'tschuldige, der osu! Server hat Unsinn gelabert und ich hab lieber ihn anstatt dich geschlagen. Frag einfach nochmal."
		+ " Falls der Server weiterhin Unsinn labert, sag [https://twitter.com/Tillerinobot @Tillerinobot] oder [http://www.reddit.com/user/tillerino /u/Tillerino] Bescheid (erwähne " + marker + "). Der soll sich darum kümmern!";
	}

	@Override
	public String noInformationForModsShort() {
		registerModification();

		return noInformationForModsShortShuffler.get(
			"Diese Mods? Denkste!",
			"Mods? Welche Mods?",
			"Nomod liebt dich."
		);
	}


	@Override
	public String noInformationForMods() {
		registerModification();

		return noInformationForModsShuffler.get(
			"Was!? Du kannst nicht wirklich glauben dass ich die Antwort dazu kenne!",
			"Ich würds dir ja erzählen, dann müsst ich dich allerdings töten.",
			"DATEN UNZUREICHEND FÜR SINNVOLLE ANTWORT."
		);
	}

	@Override
	public String unknownCommand(String command) {
		return command + "? Ich glaube dir ist nicht ganz klar wer hier der Boss ist. Du machst was ich dir sage, und ich antworte dir, wenn mir danach ist. Benutz !help, wenn dir das zu kompliziert war!";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Du Dummkopf... Du kannst dir nicht einfach irgendwelche Mods ausdenken. Wenn du nicht mal normale Dinge wie !with HR oder !with HDDT schreiben kannst, werde ich nicht mal versuchen das zu verstehen.";
	}

	@Override
	public String noLastSongInfo() {
		return "Du hast nicht mal ein Lied erwähnt. Warte, hast du versucht diese Mods an MIR zu verwenden!?";
	}

	@Override
	public String tryWithMods() {
		registerModification();

		return tryWithModsShuffler.get(
			"So ein Trottel wie du hätte gar nicht daran gedacht, das hier mit Mods zu spielen. Ein Dankeschön wäre angebracht.",								
			"Ich würde sogar fast sagen, dass du hier Mods benutzen kannst, ohne dich zum Affen zu machen.",
			"Vielleicht bist du hier dazu in der Lage, andere Mods außer NF zu benutzen. Allerdings reden wir hier immer noch von dir." //I still kinda like this one. You can decide, 
		);																																//since it's your program, but i think it get's		
	}																																	//the point across

	@Override
	public String tryWithMods(List<Mods> mods) {
		registerModification();

		String modnames = Mods.toShortNamesContinuous(mods);
		return tryWithModsListShuffler.get(
			"Benutz " + modnames + ". Sonst...",
			modnames + " beißen nicht.",
			"Schonmal von " + modnames + " gehört?"
		);
	}

	@Override
	public String excuseForError() {
		return "Hast du was gesagt? N-nicht, dass mich irgendwie interessiert, was du zu sagen hast, aber du solltest es noch mal sagen, damit ich so tun kann.";
	}

	@Override
	public String complaint() {
		return "Waaaas!? Wie kannst du nur... oh warte, diese Beatmap? Eigentlich ist die da weil ich sie hasse und ich dich auf die Probe stellen wollte. Freust du dich nicht etwas mit mir gemeinsam zu haben?";
	}
	
	@Nonnull
	@Override
	protected Response getHugResponseForHugLevel(String username, int hugLevel) {
		switch (hugLevel) {
			default:
				return new Action("Ignoriert " + username + "s Versuch einer Umarmung komplett.");
			case 0:
				return new Action("*Schlägt " + username + "*")
						.then(new Message("'tschuldige, Reflex."));
			case 1:
				return new Action("*Umarmt " + username + "*")
						.then(new Message("Wow, du bist ziemlich schlecht im Umarmen. Jemand sollte dir das mal beibringen."));
			case 2:
				return new Message("Da ist was auf deinem Rücken, du Chaot. Warte, ich machs eben weg.")
						.then(new Action("*Umarmt " + username + "*"));
			case 3:
				return new Action("*Umarmt " + username + "*")
						.then(new Message("I-ich hab nicht versucht dich zu umarmen! Ich hab nur für 'ne Sekunde mein Gleichgewicht verloren und bin auf dich gefallen."));
			case 4:
				return new Action("*Umarmt " + username + "*")
						.then(new Message("Das Schwerste beim Versuch dich zu umarmen, ist das Loslassen. Ich glaube du schwitzt zu viel."));
			case 5:
				return new Action("*Schlägt " + username + "*")
						.then(new Message("Ups... nun, du hast es wahrscheinlich sowieso verdient."));
			case 6:
				return new Action("*Umarmt " + username + "*")
						.then(new Message("Versteh mich nicht falsch, es ist nicht als würde ich dich mögen oder so..."));
			case 7:
				return new Message("Anhänglichkeit ist was schlechtes, Idiot.")
						.then(new Action("*Umarmt " + username + "*"));
			case 8:
				return new Message("I-idiot. Es i-ist so als würdest du Spaß daran haben mich zu umarmen.")
						.then(new Action("*Umarmt " + username + "*"));
			case 9:
				return new Action("*Umarmt " + username + "*")
						.then(new Message("Vergiss nicht: du bist für immer hier."));	//Reference to an old meme
			case 10:
				return new Action("*Schlägt " + username + " hart*")
						.then(new Message("Hehe. Gib's zu, du magst es!"))
						.then(new Action("*Umarmt " + username + " glücklich*"));
		}
	}

	@Override
	public String help() {
		return "Hilflos (wie immer)? Geh auf https://twitter.com/Tillerinobot für den Status und Updates, und https://github.com/Tillerino/Tillerinobot/wiki für Befehle. Was würdest du nur ohne meine Hilfe machen?";
	}

	@Override
	public String faq() {
		return "Ernsthaft, jede Antwort auf dieser Liste sollte absolut offensichtlich sein, aber es ist verständlich wenn -du- Hilfe beim Lesen brauchst: https://github.com/Tillerino/Tillerinobot/wiki/FAQ";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "'tschuldige, " + feature + " ist nur für Leute, die osu! auch spielen können. Rang " + minRank + " sollte ansonsten ausreichen, nicht dass du irgendeine Hoffnung hättest diesen jemals zu erreichen.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Was soll das sein? Schrödingers Mod? Ich habe eine Empfehlung, aber die Superposition würde kollabieren sobald sie beobachtet würde. Sowieso mag ich dich nicht genug um die Gesetze der Physik zu brechen!"; 
	}

	@Override
	public String outOfRecommendations() {
		return "WAS!? [https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do Bist du wirklich du JEDE Empfehlung, die ich dir gegeben habe, durchgegangen?] Ich k-kann mir das nicht vorstellen... nun, lass sie uns erneut durchlaufen. Sieht nicht so aus als hättest du sonst was zu tun.";
	}

	@Override
	public String notRanked() {
		return "Hmph. Diese Beatmap wird keinem mehr pp geben.";
	}

	@Override
	public Response optionalCommentOnNP(OsuApiUser apiUser, BeatmapMeta meta) {
		if (Math.random() > 0.25) {
			return new NoResponse();
		}
		PercentageEstimates estimates = meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20.0);
		if (estimates.getPP(.95) / typicalPP > 2.0) {
			return new Message("Ist das dein Ernst!? Wenn dich diese Map nicht umbringt, werde ich es machen.");
		} else if (estimates.getPP(1) / typicalPP < 0.333) {
			return new Message("Diese Map zu spielen wird mich nicht sonderlich beeindrucken... n-n-nicht dass ich das wollen würde.");
		}
		return new NoResponse();
	}
	
	@Override
	public Response optionalCommentOnWith(OsuApiUser apiUser, BeatmapMeta meta) {
		//The following checks are probably redundant, but they don't hurt anyone either.
		if (Math.random() > 0.25) {
			return new NoResponse();
		}
		PercentageEstimates estimates = meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20);
		if (estimates.getPP(.95) / typicalPP > 2.0) {
			return new Message("Idiot! Du wirst dich noch verletzen wenn du diese Mods benutzt!");
		} else if (estimates.getPP(1) / typicalPP < 0.5) {
			return new Message("Wenn du wie ein Baby behandelt werden willst, hättest du einfach fragen müssen... Nein, fang einfach an zu spielen.");
		}
		return new NoResponse();
	}
	
	@Override
	protected Response getOptionalCommentOnRecommendationResponse(int recentRecommendations) {
		switch (recentRecommendations) {
			case 7:
				return new Message("Ich hab viel Freizeit. Ich würde nie Maps raussuchen weil ich dich mag... r-r-rein hypothetisch.");
			case 17:
				return new Message("Weißt du, es ist ein Privileg so viel mit mir zu reden, kein Recht.");
			case 37:
				return new Message("Wie würdest du eigentlich dieses Spiel spielen, wenn ich dir nicht die ganze Zeit sagen würde wie?");
			case 73:
				return new Message("Ich hätte dich schon längst für Belästigung angezeigt, wenn ich dich nicht lieb... Ich hab nichts gesagt.");
			case 173:
				return new Message("Kannst mich einfach nicht allein lassen, was? Ich d-denke das ist okay. Aber wag es nicht, das jemandem zu erzählen!");
			default:
				return new NoResponse();
		}
	}

	@Override
	public String invalidAccuracy(String acc) {
		registerModification();

		return invalidAccuracyShuffler.get(
			"\"Die erste Regel ist, dass du dich nicht selbst betrügen sollst - und du bist die einfachste Person zum Betrügen.\"",
			"\"Erfolg ist die Fähigkeit, von einem Fehlschlag zum nächsten zu gehen, und dass ohne den Verlust von Enthusiasmus.\"",
			"\"Schreibe nichts der Böswilligkeit zu, was durch Dummheit hinreichend erklärbar ist\"",
			"\"Der einzige wirkliche Fehler ist der, von dem wir nichts lernen.\"",
			"\"Zwei Dinge sind unendlich, das Universum und die menschliche Dummheit, aber bei dem Universum bin ich mir noch nicht ganz sicher.\"",
			"\"Manchmal will ein Mann dumm sein, wenn es ihn etwas machen lässt, was ihm seine Klugheit verbietet.\"",
			"\"Ernsthaft, wenn du noch langsamer gehst, gehst du rückwärts.\""
		);
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		registerModification();

		return new Message(optionalCommentOnLanguageShuffler.get(
			"[https://osu.ppy.sh/u/3135278 MrMorkat] hat mir dabei geholfen Deutsch zu lernen. Aber welche Art von Idiot will einen Tsundere Roboter auf Deutsch!? Das ist ernsthaft die dümmste Idee, die ich je gehört habe!",
			//"[https://osu.ppy.sh/u/3135278 MrMorkat] hat mir dabei geholfen Deutsch zu lernen. Das wollte ich dir immer schon in einer anderen Sprache sagen: Ich lieb... Ich hab nichts gesagt!", I kinda not like this
			"[https://osu.ppy.sh/u/3135278 MrMorkat] hat mir dabei geholfen Deutsch zu lernen. Aber ich hab das nur gemacht, weil ich es wollte. Das hat nichts mit dir zu tun!"
		));
	}


	@Override
	protected String getInvalidChoiceResponse(String invalid, String choices) {
		return "Was soll \"" + invalid + "\" bitte bedeuten!? Falls zwei Finger zu viel sind, versuch doch jeden Buchstaben zu singletappen: " + choices;
	}

	@Override
	public String setFormat() {
		return "Drei Worte: !set option_name value_to_set. Versuch !help, falls dir drei-Wort-Sätze zu kompliziert sind.";
	}
	
	@Override
	public String apiTimeoutException() {
		return new Deutsch().apiTimeoutException();
	}
	
	@Override
	public String noRecentPlays() {
		return new Deutsch().noRecentPlays();
	}
	
	@Override
	public String isSetId() {
		return new Deutsch().isSetId();
	}
}

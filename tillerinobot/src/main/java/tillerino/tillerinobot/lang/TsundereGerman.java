package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.NoResponse;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.diff.PercentageEstimates;

public class TsundereGerman extends TsundereBase implements Language {
	
	//Random object, used in StringShuffler
	static final Random rnd = new Random();
	//Recent counters, reset if inactive for a while
	int recentRecommendations = 0;
	int recentHugs = 0;
	
	StringShuffler welcomeUserShortShuffler = new StringShuffler(rnd);
	StringShuffler welcomeUserShuffler = new StringShuffler(rnd);
	StringShuffler welcomeUserLongShuffler = new StringShuffler(rnd);
	
	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		String username = apiUser.getUserName();
		String greeting = "";
		//Greetings for <4 minutes, normal, and >4 days
		if (inactiveTime < 4 * 60 * 1000) {
			greeting = welcomeUserShortShuffler.get(
				"Was soll das hier sein, das Kuckuckspiel?",
				"Das waren nicht mal fünf Minuten...",
				"Heute hat " + username + "an seinem Verschwindungszauber geübt."
			);
		} else if (inactiveTime < 4l * 24 * 60 * 60 * 1000) {
			greeting = welcomeUserShuffler.get(
				"Wieder da? Ich bin nur hier weil ich nix zu tun hab! Nichts weiter!",
				"H-hey...",
				//"♫ Home again, home again, jiggety-jig ♫ .... What?",
				"♫ Zuhause, zuhause, lalalalala ♫ .... Was?",
				"Stell dir vor! Ich habe endlich deine Bestimmung gefunden. Sie gehört allerdings nicht zu der Art von Dingen, die ich gerne teile!",
				"Du hast doch nicht etwa mit anderen Chatbots geredet, oder?",
				//"Huh? What are you doing here!?",
				"Huch? Was machst du denn hier!?",
				//username + ", go do something stupid so I can scold you for it."
				"Mach was dummes " + username + ", damit ich dich dafür bestrafen kann."
			);
		} else {
			greeting = welcomeUserLongShuffler.get(
				"Wo warst du " + username + "!? N-nicht das ich dich vermisst hätte oder so...",
				"Wie war dein Urlaub " + username + "?",
				"Ugh! Weißt du wie lang " + inactiveTime + " Millisekunden ist!?"
			);
		}
		//Recent counter reset (4 hours)
		if (inactiveTime > 4 * 60 * 60 * 1000) {
			recentRecommendations = 0;
			recentHugs = 0;
		}

		setChanged(true);

		return new Message(greeting);
	}

	StringShuffler unknownBeatmapShuffler = new StringShuffler(rnd);

	@Override
	public String unknownBeatmap() {
		setChanged(true);

		return unknownBeatmapShuffler.get(
			"Bist du dumm? Niemand würde diese Map spielen!",
			"Ach wirklich? Nie gehört!",
			//"Yeah right, call me when you manage to get pp with that."
			"Ja genau. Sag mir Bescheid wenn du damit pp erhalten hast."
		);	
	}

	@Override
	public String internalException(String marker) {
		return "Huch? Warum funktioniert das nicht? Das war bestimmt dein Fehler!"
		+ " Falls dies weiterhin vorkommt leite folgendes an [https://twitter.com/Tillerinobot @Tillerinobot] oder [http://www.reddit.com/user/tillerino /u/Tillerino]weiter: "+ marker + ".";
	}

	@Override
	public String externalException(String marker) {
		return //"Sorry, the osu! server was saying some idiotic nonsense and I felt like slapping them instead of you. Try asking whatever it was again."
		"'tschuldige, der osu! Server hat Unsinn gelabert und ich hab lieber ihn anstatt dich geschlagen. Frag einfach nochmal."
		+ " Falls der Server weiterhin Unsinn labert, sag [https://twitter.com/Tillerinobot @Tillerinobot] oder [http://www.reddit.com/user/tillerino /u/Tillerino] Bescheid (erwähne " + marker + "). Die sollen sich darum kümmern!";
	}

	StringShuffler noInformationForModsShortShuffler = new StringShuffler(rnd);
	
	@Override
	public String noInformationForModsShort() {
		setChanged(true);

		return noInformationForModsShortShuffler.get(
			"Diese Mods? Denkste!",
			"Mods? Welche Mods?",
			"Nomod liebt dich."
		);
	}

	StringShuffler noInformationForModsShuffler = new StringShuffler(rnd);
	
	@Override
	public String noInformationForMods() {
		setChanged(true);

		return noInformationForModsShuffler.get(
			"Was!? Du kannst nicht wirklich glauben dass ich die Antwort dazu kenne!",
			"Ich würds dir ja erzählen, dann müsst ich dich allerdings töten.",
			"NICHT AUSREICHEND DATEN FÜR EINE SINNVOLLE ANTWORT."
		);
	}

	@Override
	public String unknownCommand(String command) {
		return command + "? Ich glaube dir ist nicht ganz klar wer hier der Boss ist. Du machst was ich dir sage, und ich antworte dir wenn mir danach ist. Benutz !help wenn dir das zu kompliziert war!";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Du Dummkopf... du kannst dir nicht einfach irgendwelche Mods ausdenken. Wenn du nicht mal normale Dinge wie !with HR oder !with HDDT schreiben kannst, werde ich nicht mal versuchen das zu verstehen.";
	}

	@Override
	public String noLastSongInfo() {
		return "Du hast nicht mal ein Lied erwähnt. Warte, hast du versucht diese Mods an MIR zu verwenden!?";
	}

	StringShuffler tryWithModsShuffler = new StringShuffler(rnd);

	@Override
	public String tryWithMods() {
		setChanged(true);

		return tryWithModsShuffler.get(
			"An idiot like you wouldn't know to try this with mods. You should thank me.",								//Need help with this one
			"Ich würde sogar fast sagen, dass du hier Mods benutzen kannst, ohne wie ein kompletter Idiot auszusehen.",
			"Vielleicht bist du hier dazu in der Lage andere Mods außer NF zu benutzen. Allerdings reden wir hier immer noch von dir."
		);
	}

	StringShuffler tryWithModsListShuffler = new StringShuffler(rnd);
	
	@Override
	public String tryWithMods(List<Mods> mods) {
		setChanged(true);

		String modnames = Mods.toShortNamesContinuous(mods);
		return tryWithModsListShuffler.get(
			"Benutz " + modnames + ". Sonst...",
			modnames + " beißen nicht.",
			"Schonmal von " + modnames + " gehört?"
		);
	}

	@Override
	public String excuseForError() {
			  //Did you say something? It's not l-like I care what you have to say, but you should say it again so you can pretend I do."
		return "Hast du was gesagt? Nicht dass m-mich das interessieren würde wenn du was zu sagen hast, aber du solltest es nochmal sagen, damit du so tun kannst als würde es.";	//This somehow isn't showing the original spirit
	}

	@Override
	public String complaint() {
		return "Waaaas!? Wie kannst du nur... oh warte, diese Beatmap? Eigentlich ist die da weil ich sie hasse und ich dich testen wollte. Bist du nicht froh etwas mit mir gemeinsam zu haben?";
	}
	
	@Override
	public Response hug(OsuApiUser apiUser) {
		setChanged(true);
		//Responses move from tsun to dere with more hug attempts and recommendations
		recentHugs++;
		int baseLevel = (int)(Math.log(recentHugs) / Math.log(2.236) + Math.log(recentRecommendations+1) / Math.log(5)); //Sum logs base sqrt(5) and 5
		int hugLevel = (baseLevel<8?baseLevel:8) + rnd.nextInt(3) + rnd.nextInt(3) - 2;  //Ranges from -2 to 10
		String username = apiUser.getUserName();
		switch (hugLevel) {
			default:
				return new Action("Ignoriert " + username + "'s Versuch einer Umarmung komplett.");
			case 0:
				return new Action("*Schlägt " + username + "*")
					.then(new Message("'tschuldige, Reflex."));
			case 1:
				return new Action("*Umarmt " + username + "*")
					.then(new Message("Wow, du bist ziemlich schlecht in Umarmungen. Jemand sollte es dir beibringen."));
			case 2:
				return new Message("Da ist was auf deinem Rücken, du Chaot. Warte, ich machs eben weg.")
					.then(new Action("*Umarmt " + username + "*"));
			case 3:
				return new Action("*Umarmt " + username + "*")
					.then(new Message("I-ich hab nicht versucht dich zu umarmen! Ich hab nur für 'ne Sekunde mein Gleichgewicht verloren und bin auf dich gefallen."));
			case 4:
				return new Action("*Umarmt " + username + "*")
					.then(new Message("Das schlimmste beim Versuch dich zu umarmen ist das loslassen. Ich glaube du schwitzt zu viel."));
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
					.then(new Message("Vergiss nicht: du bist für immer hier."));	//didn't understand the meaning behind the original: "Don't forget: You are here forever."
			case 10:
				return new Action("*Schlägt " + username + " hart*")
					.then(new Message("Hehe. Du weißt, du magst es."))
					.then(new Action("*Umarmt " + username + " glücklich*"));
		}
	}

	@Override
	public String help() {
		return "Hilflos (wie immer)? Geh auf https://twitter.com/Tillerinobot für den Status und Updates, und https://github.com/Tillerino/Tillerinobot/wiki für Befehle. Was würdest du nur ohne meine Hilfe machen?";
	}

	@Override
	public String faq() {
		return "Ernsthaft, jede Antwort auf dieser Liste sollte intuitiv auf der Hand liegen, aber es ist verständlich wenn -du- Hilfe brauchst sie zu lesen: https://github.com/Tillerino/Tillerinobot/wiki/FAQ"; //doesn't rly make sense in the end
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "'tschuldige, " + feature + " ist nur für Leute die osu! auch spielen können. Das Erreichen von Rang " + minRank + " sollte funktionieren, nicht dass du irgendeine Hoffnung hättest diesen jemals zu erreichen.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Was soll das sein? Schrödinger's Mod? I have a recommendation, but the superposition would collapse as soon as it was observed. It's not like I like you enough to break the laws of physics anyways!"; //lol, don't know how to translate the reference so it's still understood as a reference
	}

	@Override
	public String outOfRecommendations() {
		return "WAS!? [https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do Bist du wirklich du JEDE Empfehlung die ich dir gegeben habe durchgegangen?] Ich k-kann mir das nicht vorstellen... nun, lass sie uns erneut durchlaufen. Sieht nicht so aus als hättest du sonst was zu tun.";
	}

	@Override
	public String notRanked() {
		return "Hmph. Diese Beatmap wird keinem mehr pp geben.";
	}

	StringShuffler optionalCommentOnNPHardShuffler = new StringShuffler(rnd);
	StringShuffler optionalCommentOnNPEasyShuffler = new StringShuffler(rnd);

	@Override
	public Response optionalCommentOnNP(OsuApiUser apiUser, BeatmapMeta meta) {
		if (Math.random() > 0.25) {
			return new NoResponse();
		}
		PercentageEstimates estimates = meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20.0);
		if (estimates.getPPForAcc(.95) / typicalPP > 2.0) {
			return new Message("Ist das dein Ernst!? Wenn dich diese Map nicht tötet werde ich es machen.");
		} else if (estimates.getPPForAcc(1) / typicalPP < 0.333) {
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
		if (estimates.getPPForAcc(.95) / typicalPP > 2.0) {
			return new Message("Idiot! Du wirst dich noch verletzen wenn du diese Mods benutzt!");
		} else if (estimates.getPPForAcc(1) / typicalPP < 0.5) {
			return new Message("Wenn du wie ein Baby behandlt werden willst, hättest du einfach fragen müssen... nein, fang einfach an zu spielen.");
		}
		return new NoResponse();
	}
	
	@Override
	public Response optionalCommentOnRecommendation(OsuApiUser apiUser, Recommendation meta) {
		setChanged(true);

		recentRecommendations++;
		if(recentRecommendations == 7) {
			return new Message("Ich hab viel Freizeit. Ich würde nie Maps raussuchen weil ich dich mag... r-r-rein hypothetisch.");
		} else if(recentRecommendations == 17) {
			return new Message("Weißt du, es ist ein Privileg so viel mit mir zu reden, kein Recht.");
		} else if(recentRecommendations == 37) {
			return new Message("Wie würdest du eigentlich dieses Spiel spielen wenn ich dir nicht die ganze Zeit sagen würde wie?");
		} else if(recentRecommendations == 73) {
			return new Message("Ich hätte dich schon längst für Belästigung angezeigt wenn ich dich nicht lieb... Ich hab nichts gesagt.");
		} else if(recentRecommendations == 173) {
			return new Message("Kannst mich einfach nicht allein lassen, was? Ich d-denke das ist okay. Aber wag es nicht das jemandem zu erzählen!");
		}
		return new NoResponse();
	}
	
	transient boolean changed;

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	StringShuffler invalidAccuracyShuffler = new StringShuffler(rnd);
	
	@Override
	public String invalidAccuracy(String acc) {
		setChanged(true);

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

	StringShuffler optionalCommentOnLanguageShuffler = new StringShuffler(rnd);

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		setChanged(true);

		return new Message(optionalCommentOnLanguageShuffler.get(
			"MrMorkat hat mir dabei geholfen Deutsch zu lernen. Aber welche Art von Idiot will einen Tsundere Roboter auf Deutsch!? Das ist ernsthaft die dümmste Idee die ich je gehört habe!",
			"MrMorkat hat mir dabei geholfen Deutsch zu lernen. Das wollte ich dir immer schon in einer anderen Sprache sagen: Ich lieb... Ich hab nichts gesagt!",
			"MrMorkat hat mir dabei geholfen Deutsch zu lernen. Aber ich hab das nur gemacht weil ich es wollte. Das hat nichts mit dir zu tun!"
		));
	}

	int invalidRecommendationParameterCount = 0;

	@Override
	public String invalidChoice(String invalid, String choices) {
		if (choices.contains("[nomod]")) {
			// recommendation parameter was off
			setChanged(true);
			/*
			 * we'll give three fake recommendations and then one proper error
			 * message. non-randomness required for unit test.
			 */
			if (invalidRecommendationParameterCount++ % 4 < 3) {
				return unknownRecommendationParameter();
			}
		}
		return "Was soll \"" + invalid + "\" bitte bedeuten!? Falls zwei Finger zu viel sind, versuch doch jeden Buchstaben zu singletappen: " + choices;
	}

	@Override
	public String setFormat() {
		return "Drei Worte: !set option_name value_to_set. Versuch !help falls dir drei-Wort-Sätze zu kompliziert sind.";
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

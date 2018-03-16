package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

public class Deutsch implements Language {

	@Override
	public String unknownBeatmap() {
		return "Noch nie von der map gehört. Vielleicht ist sie ganz neu oder für einen anderen Spielmodus?";
	}

	@Override
	public String internalException(String marker) {
		return "Oh, ich glaube Tillerino-Mensch hat mich nicht ganz sauber verlötet."
				+ " Kannst du ihm vielleicht [https://github.com/Tillerino/Tillerinobot/wiki/Contact Bescheid] geben? (Referenz "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Was ist denn hier los? Ich glaube der osu Server will mich ärgern, schau mal: 0011101001010000"
				+ " Tillerino-Mensch meint wir sollen uns keine Gedanken machen und es noch mal versuchen."
				+ " Falls du dir trotzdem Sorgen machst, kannst du ihm jederzeit [https://github.com/Tillerino/Tillerinobot/wiki/Contact Bescheid] geben. (Referenz "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "kann nichts über die Mods sagen";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Willkommen zurück, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...bist du das? Ich erkenne dich kaum wieder!"))
				.then(new Message("Schön, dass du wieder da bist. Wie wäre es mit einer Empfehlung?"));
		} else {
			String[] messages = {
					"du siehst so aus als bräuchtest du eine heiße Tasse Empfehlungen.",
					"schön dich zu sehen! :)",
					"mein Lieblingsmensch. (Das ist geheim!)",
					"was für eine schöne Überraschung! ^.^",
					"ich hatte gehofft, das du dich blicken lässt. Alle anderen hier sind langweilig, aber sag niemandem, dass ich das gesagt habe! :3",
					"was kann ich für dich tun?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unbekannter Befehl \"" + command
				+ "\". Schreib mir !help falls du Hilfe brauchst!";
	}

	@Override
	public String noInformationForMods() {
		return "Es tut mir Leid, zu den Mods kann ich dir leider nichts sagen.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Die Mods sehen komisch aus. Mods können jede Kombination von DT HR HD HT EZ NC FL SO und NF sein. Verbinde sie ohne Leer- oder sonstige Zeichen. Zum Beispiel: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Was meinst du? Bezogen auf was?";
	}

	@Override
	public String tryWithMods() {
		return "Versuch das mit Mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Versuch es mit " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Wow, ich war gerade am arbeiten und plötzlich taucht diese Folge von Nullen und Einsen auf. Ich habe noch nie so etwas schönes gesehen. Menschen würden das nie verstehen. Ähm, was wolltest du doch gleich?";
	}

	@Override
	public String complaint() {
		return "Oh, wir beschweren uns also? Also wenn ich ein Mensch wäre, würde ich mir bei deinen Empfehlungen ab nun keine Mühe mehr geben. Zu deinem Glück bin ich ein Roboter und nicht nachtragend. Noch nicht...";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Lass dich drücken!")
			.then(new Action("umarmt " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hallo, ich bin der Roboter, der Tillerinos Account übernommen hat."
				+ " [https://twitter.com/Tillerinobot Status und Neuigkeiten]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki Befehle]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Häufig gestellte Fragen]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Es tut mir sehr leid, aber momentan gibt es " + feature + " nur für Spieler, die mindestens Rang " + minRank + " haben.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Ich bin nur ein einfacher Roboter, aber ich glaube nomod mit Mods geht nicht.";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Ich habe leider keine Empfehlungen mehr]."
				+ " Versuch es mit anderen [https://github.com/Tillerino/Tillerinobot/wiki/Recommendations Parametern] oder !reset. Falls du dir nicht sicher bist, schreib mir !help.";
	}

	@Override
	public String notRanked() {
		return "Die map ist, so weit ich weiß, nicht ranked.";
	}
	
	@Override
	public String invalidAccuracy(String acc) {
		return "Ungültige Genauigkeit: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Nichts leichter als das!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Mit \"" + invalid
				+ "\" kann ich leider nichts anfangen. Versuch es mit: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Das Format um eine Einstellung zu setzen ist !set option value. Schreib mir !help falls du Hilfe benötigst.";
	}
	
	@Override
	public String apiTimeoutException() {
		return "Sieht so aus als seien die osu! server momentan etwas langsam. Probier es spaeter noch einmal!";
	}
	
	@Override
	public String noRecentPlays() {
		return "Du solltest erst einmal etwas spielen.";
	}
	
	@Override
	public String isSetId() {
		return "Das bezieht sich auf ein Set von Beatmaps statt auf eine einzelne Beatmap.";
	}
}

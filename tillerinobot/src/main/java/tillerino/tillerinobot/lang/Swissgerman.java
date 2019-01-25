package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

public class Swissgerman implements Language {

	@Override
	public String unknownBeatmap() {
		return "No nie vo dere Map ghört. Vilicht isch sie ganz neu oder für en andere Spiilmodus?";
	}

	@Override
	public String internalException(String marker) {
		return "Oh, ich glaube de Tillerino-Mensch het mich nöd ganz sauber verlötet."
				+ " Chasch ihm echt [https://github.com/Tillerino/Tillerinobot/wiki/Contact Bscheid] geh? (Referenz "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Was isch denn da los? Ich glaube de osu Server will mich ärgere, lueg mal: 0011101001010000"
				+ " De Tillerino-Mensch meint mir sötted eus kei Gedanke mache unds nomal versueche."
				+ " Falls du dir trotzdem Sorge machsch, chasch ihm jederziit [https://github.com/Tillerino/Tillerinobot/wiki/Contact Bscheid] geh. (Referenz "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "chan nüt über d Mods säge.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Willkomme zrugg, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...bisch du das? Ich erkenn dich fasch nüme!"))
				.then(new Message("Schön, dass du wieder da bisch. Wie wärs mitere Empfehlig?"));
		} else {
			String[] messages = {
					"du gsehsch so us als brüchtisch e heissi Tasse Empfehlige.",
					"schön dich z gseh! :)",
					"min Lieblingsmensch. (Das isch geheim!)",
					"was für e schöni Überraschig! ^.^",
					"ich han ghofft, das du dich blicke lahsch. Alli andere da sind langwiilig, aber seg niemertem, dass ich das gseit han! :3",
					"was chani für dich mache?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unbekannte Befehl \"" + command
				+ "\". Schriib mir !help falls du Hilf bruchsch!";
	}

	@Override
	public String noInformationForMods() {
		return "Es tuet mir Leid, zu dene Mods chani dir leider nüt sege.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Die Mods gsehnd komisch us. Mods chönd jedi Kombination vo DT HR HD HT EZ NC FL SO und NF si. Verbind sie ohni Leer- und suschtigi Zeiche. Zum Bispil: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Was meinsch? Bezoge uf was?";
	}

	@Override
	public String tryWithMods() {
		return "Versuech das mit Mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Versuechs mit " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Wow, ich bin grad am schaffe gsi und plötzlich isch die Folg vo Nuller und Einer uftaucht. Ich han nonie so öppis schöns gseh. Mensche würdet das nie verstah. Ähm, was hesch scho wieder welle?";
	}

	@Override
	public String complaint() {
		return "Oh, beschwersch dich also? Also wenn ich en Mensch wär, würd ich mir bi dine Empfehlige ab jetzt kei Müeh me geh. Zu dim Glück bin ich en Roboter und nöd nachträgend. Nonig...";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("La dich umarme!")
			.then(new Action("umarmt " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hallo, ich bin de Roboter wo em Tillerino sin Account überno het."
				+ " [https://twitter.com/Tillerinobot Status und Neuigkeite]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki Befehl]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Kontakt]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Hüfig gestellti Frage]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Es tuet mir sehr leid, aber momentan gits " + feature + " nur für Spieler, wo mindestens Rang " + minRank + " sind.";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Ich bin nur en eifache Roboter, aber ich glaube Nomod mit Mods gaht nöd.";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Ich han leider kei Empfehlige me]."
				+ " Versuechs mit anderne [https://github.com/Tillerino/Tillerinobot/wiki/Recommendations Parameter] oder !reset. Falls du dir nöd sicher bisch, schriib mir !help.";
	}

	@Override
	public String notRanked() {
		return "Die map isch, so wiit ich weiss, nöd granket.";
	}
	
	@Override
	public String invalidAccuracy(String acc) {
		return "Ungültigi Genauigkeit: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("De Maurice het mir Schwiizerdütsch bihbracht. :D");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Mit \"" + invalid
				+ "\" chan ich leider nüt ahgfange. Versuechs mit: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "S Format zum e Ihstellig setze isch !set option value. Schriib mir !help falls du Hilf bruchsch.";
	}
	
	@Override
	public String apiTimeoutException() {
		return "Gseht so us als wäred d osu! Server chli langsam im Moment. Probiers spöter nomal!";
	}
	
	@Override
	public String noRecentPlays() {
		return "Du sötsch zersch chli spile.";
	}
	
	@Override
	public String isSetId() {
		return "Das bezieht sich uf es Set vo Beatmaps anstatt uf e einzelni Beatmap.";
	}
}

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
 * @author Underforest https://osu.ppy.sh/u/6753180
 */
public class Catalan extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Ho sento, no conec aquest mapa. Potser és molt recent, molt difícil, no està rankeado o l'modo de joc no és osu! estàndard.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Sembla que Tillerino humà va espatllar meu cablejat."
				+ " Si no s'adona aviat, pots [https://github.com/Tillerino/Tillerinobot/wiki/Contact informar]? (referència "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Què està passant? Estic rebent tonteries per part del servidor de osu. Saps què vol dir això? 0011101001010000"
				+ " Tillerino humà diu que això no és res de què preocupar-se i que hauríem intentar-ho de nou."
				+ " Si per alguna raó estàs realment preocupat, pots [https://github.com/Tillerino/Tillerinobot/wiki/Contact comunicar]. (referència "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "no hi ha dades per a aquests mods";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if (inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Benvingut de nou, " + apiUser.getUserName() + ".");
		} else if (inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...ets tu? Quant de temps!"))
				.then(new Message("És genial tenir-te de tornada. Puc donar-te una recomanació?"));
		} else {
			String[] messages = {
					"sembla que necessites una recomanació.",
					"és sorprenent veure't! :)",
					"meu humà favorit. (No l'hi diguis als altres humans!)",
					"quina agradable sorpresa! ^.^",
					"Estava esperant que tornessis. Tots els altres humans són uns avorrits, però no els diguis res! :3",
					"què t'agradaria fer avui?",
			};

			Random random = new Random();

			String message = messages[random.nextInt(messages.length)];

			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Comandament desconegut \"" + command
				+ "\". Escriu !help si necessites ajuda!";
	}

	@Override
	public String noInformationForMods() {
		return "Ho sento, no puc donar informació sobre aquests mods ara.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Aquests mods no estan bé. Els mods poden ser una combinació de DT HR HD HT EZ NC FL SO NF. Combina'ls sense espais o caràcters especials. Exemple: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "No recordo haver-te donat informació d'alguna cançó...";
	}

	@Override
	public String tryWithMods() {
		return "Prova aquesta ruta amb alguns mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prova aquesta ruta amb " + Mods.toShortNamesContinuous(mods) + "!";
	}

	@Override
	public String excuseForError() {
		return "Ho sento, és que hi havia una bella seqüència de zeros i uns i em vaig distreure, què necessites?";
	}

	@Override
	public String complaint() {
		return "Teu queixa s'ha arxivat. Tillerino li hechará una ullada quan pugui.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Vine aquí, tu!")
			.then(new Action("abraça " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hola! Sóc el robot que va assassinar Tillerino i va robar el seu compte. No és cert, però faig servir el seu compte sovint."
				+ " [https://twitter.com/Tillerinobot estat i actualitzacions]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki comandaments]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contacte]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Preguntes freqüents]";
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Ho sento, ara mateix " + feature + " només està disponible per a jugadors que hagin superat el rang " + minRank + ".";
	}

	@Override
	public String mixedNomodAndMods() {
		return "Què vols dir amb mods i després sense mods?";
	}

	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " És tot el que puc recomanar]."
				+ " Prova altres opcions de recomanació o utilitza !reset. Si no estàs segur, prova amb !help.";
	}

	@Override
	public String notRanked() {
		return "Sembla que aquest mapa no està rankeado.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Precisió invàlida: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("[https://osu.ppy.sh/u/6753180 Underforest] em va ajudar a aprendre català");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Ho sento, però \"" + invalid
				+ "\" no existeix. Prova el següent: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "La sintaxi per a establir un paràmetre és !set opció valor. Prova !help si necessites més indicacions.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "Els servidors de osu! estan molt lents ara mateix, pel que no puc ajudar-te amb res a hores d'ara. ";
		return message + apiTimeoutShuffler.get(
				"I digues-me... quan va ser l'última vegada que vas parlar amb teva àvia?",
				"Què tal si ordenes una mica teva habitació i després em preguntes novament?",
				"Aposto a que et encantaria fer una passejada, ja saps ... fora?",
				"Estic segur que tens altres coses a fer. Què et sembla fer-les ara mateix?",
				"Sembla que necessites una migdiada de totes maneres.",
				"Fes una ullada a aquesta pàgina interessant en [https://en.wikipedia.org/wiki/Special:Random wikipedia]!",
				"Revisem si algú bo està [http://www.twitch.tv/directory/game/Osu! transmetent] ara!",
				"Mira, aquí hi ha altre [http://dagobah.net/flash/Cursor_Invisible.swf joc] en el que potser siguis pèssim!",
				"Això hauria de donar-te temps d'estudiar [https://github.com/Tillerino/Tillerinobot/wiki meu manual].",
				"Tranquil, aquests [https://www.reddit.com/r/osugame divertits memes] haurien de passar el temps.",
				"Mentre estàs avorrit, prova [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Pregunta divertida: Si el teu disc dur es trenqués ara mateix, quants dades personals es perdrien per sempre?",
				"Llavors, alguna vegada vas intentar fer el [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"Pots fer altra cosa o ens podem quedar mirant-nos als ulls. Un a l'altre. Silenciosament."
				);
	}

	@Override
	public String noRecentPlays() {
		return "No has jugat recentment.";
	}
	
	@Override
	public String isSetId() {
		return "Això fa referència un grup de beatmaps, no un beatmap únic.";
	}
}

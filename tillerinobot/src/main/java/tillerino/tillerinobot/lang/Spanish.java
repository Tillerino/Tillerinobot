package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author Sibchil https://osu.ppy.sh/u/3988045 https://github.com/sibchil
 * @author Zarkinox arichtermohr@gmail.com https://osu.ppy.sh/u/2743036 https://github.com/zarkinox
 */
public class Spanish extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Lo siento, no conozco ese mapa. Tal vez es muy reciente, muy difícil, no está rankeado o el modo no es osu! estándar.";
	}

	@Override
	public String internalException(String marker) {
		return "Ehh... Parece que Tillerino humano estropeó mi cableado."
		                + " Si no se da cuenta pronto, ¿podrías [https://github.com/Tillerino/Tillerinobot/wiki/Contact informarle]? (código de referencia "
		                + marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "¿Qué está pasando? Solo recibo disparates del servidor de osu. ¿Podrías decirme qué se supone que significa esto? 0011101001010000"
		                + " Tillerino humano dice que esto no es nada de qué preocuparse y que deberíamos intentarlo de nuevo."
		                + " Si por alguna razón estás realmente preocupado, puedes [https://github.com/Tillerino/Tillerinobot/wiki/Contact comunicárselo]. (código de referencia "
		                + marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "no hay datos para los mods solicitados";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Bienvenid@, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...¿eres tú? ¡Ha pasado tanto tiempo!"))
				.then(new Message("Es genial tenerte de vuelta. ¿Puedo ofrecerte una recomendación?"));
		} else {
			String[] messages = {
					"me parece que necesitas una recomendación.",
					"¡cómo me alegro de verte! :)",
					"mi humano favorito. (¡No se lo digas a los otros humanos!)",
					"¡qué agradable sorpresa! ^.^",
					"Estaba esperando que aparecieses. Todos los otros humanos son unos aburridos, ¡pero no les digas nada! :3",
					"¿qué te apetece hacer hoy?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "comando desconocido \"" + command
				+ "\". ¡Escribe !help si necesitas ayuda!";
	}

	@Override
	public String noInformationForMods() {
	    return "Lo siento, pero no puedo proporcionar información sobre esos mods ahora mismo.";
	}

	@Override
	public String malformattedMods(String mods) {
	    return "Esos mods no parecen correctos. Los mods pueden ser cualquier combinación de DT HR HD HT EZ NC FL SO NF. Combínalos sin espacios o caracteres especiales. Ejemplo: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
	    return "No recuerdo haberte dado información sobre ninguna canción...";
	}

	@Override
	public String tryWithMods() {
		return "¡Prueba este mapa con algunos mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prueba este mapa con " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Lo siento, había una hermosa secuencia de unos y ceros y me distraje. ¿Qué es lo que querías?";
	}

	@Override
	public String complaint() {
		return "Tu queja se ha archivado. Tillerino le hechará un vistazo cuando pueda.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("¡Ven aquí!")
			.then(new Action("Abraza a " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "¡Hola! Soy el robot que mató a Tillerino y tomó el control de su cuenta. Es broma, pero uso su cuenta muy a menudo."
				+ " [https://twitter.com/Tillerinobot estado y actualizaciones]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki comandos]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contacto]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Preguntas frecuentes]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Lo siento, actualmente " + feature + " está solo disponible para jugadores que hayan superado el rango" + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "¿A qué te refieres con 'sin mods (nomod) y con mods'?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Te he recomendado todo lo que se me ocurre]."
				+ " Prueba otras opciones de recomendación o usa !reset. Si no estás seguro, prueba con !help.";
	}

	@Override
	public String notRanked() {
		return "Parece que este mapa no está rankeado.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Precisión inválida: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Sasakura, Underforest y [https://osu.ppy.sh/u/2743036 Zarkinox] me ayudaron a aprender español :3");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Lo siento, pero \"" + invalid
				+ "\" no computa. Prueba estos: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "La sintaxis para establecer un parámetro es !set opción valor. Prueba !help si necesitas más indicaciones.";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);

	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "Los servidores de osu! están muy lentos ahora mismo, por lo que no puedo ayudarte con nada en estos momentos. ";
		return message + apiTimeoutShuffler.get(
				"Dime, ¿cuándo fue la última vez que hablaste con tu abuela?",
				"¿Qué te parece si ordenas un poco tu habitación y luego me preguntas otra vez?",
				"Apuesto a que te encantaría dar un paseo, ya sabes... fuera de casa y eso.",
				"Estoy seguro de que tienes otras cosas que hacer. ¿Qué te parece hacerlas ahora mismo?",
				"De todas formas, parece que necesitas una siesta.",
				"¡Echemos un vistazo a esta página súper interesante en [https://es.wikipedia.org/wiki/Special:Random wikipedia]!",
				"¡Vamos a ver si hay alguien bueno [http://www.twitch.tv/directory/game/Osu! en directo] ahora mismo!",
				"¡Mira, aquí hay otro [http://dagobah.net/flash/Cursor_Invisible.swf juego] en el que probablemente seas malísimo!",
				"Esto debería de darte tiempo más que suficiente para que te estudides [https://github.com/Tillerino/Tillerinobot/wiki mi manual].",
				"No te preocupes, estos [https://www.reddit.com/r/osugame buenos memes] deberían ayudarte a pasar el rato.",
				"Si te estás aburriendo, prueba [http://gabrielecirulli.github.io/2048/ 2048].",
				"Pregunta curiosa: Si tu disco duro se rompiese ahora mismo, ¿qué cantidad de tus datos personales se perderían para siempre?",
				"¿Has intentado alguna vez el [https://www.google.de/search?q=bring%20sally%20up%20push%20up%20challenge 'sally up push up challenge']?",
				"Puedes ir a hacer otra cosa o nos podemos quedar mirándonos a los ojos. Uno al otro. En completo silencio."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "No te he visto jugar últimamente.";
	}
	
	@Override
	public String isSetId() {
		return "Esto hace referencia a un grupo de mapas, no a uno solo.";
	}
}

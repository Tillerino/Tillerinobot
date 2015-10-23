package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * @author Sibchil https://osu.ppy.sh/u/3988045 https://github.com/sibchil
 */
public class Spanish implements Language {

	@Override
	public String unknownBeatmap() {
		return "Lo siento, no conozco ese mapa. Es muy reciente, muy difícil, unranked o modo de osu! no estándar.";
	}

	@Override
	public String internalException(String marker) {
		return "Ehh... Parece que Tillerino humano estropeó mi cableado."
		                + " Si no se da cuenta pronto, ¿podrías [https://github.com/Tillerino/Tillerinobot/wiki/Contact informarle]? (código de referencia "
		                + marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "¿Qué está pasando? Solo recibo disparates del servidor de osu. ¿Podrías decirme que se supone que significa esto? 0011101001010000"
		                + " Tillerino humano dice que no hay nada por qué preocuparse y que deberíamos intentarlo de nuevo."
		                + " Si estás preocupado por alguna razón, puedes [https://github.com/Tillerino/Tillerinobot/wiki/Contact comunicárselo]. (código de referencia "
		                + marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "no hay datos para los mods solicitados";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Bienvenid@, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...¿eres tú? ¡Ha pasado tanto tiempo!");
			user.message("Es genial tenerte de vuelta. ¿Puedo ofrecerte una recomendación?");
		} else {
			String[] messages = {
					"me parece que necesitas una recomendación.",
					"¡cómo me alegro de verte! :)",
					"mi humano favorito. (¡No se lo digas a los otros humanos!)",
					"¡que agradable sorpresa! ^.^",
					"Tenía la esperanza de que te dejarías ver. Todos los demás humanos son patéticos, ¡pero no les cuentes lo que dije! :3",
					"¿qué te apetece hacer hoy?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "comando desconocido \"" + command
				+ "\". ¡Escribe !help si necesitas ayuda!";
	}

	@Override
	public String noInformationForMods() {
	    return "Lo siento, no puedo proporcionar información sobre esos mods ahora mismo.";
	}

	@Override
	public String malformattedMods(String mods) {
	    return "Esos mods no parecen correctos. Los mods pueden ser cualquier combinación de DT HR HD HT EZ NC FL SO NF. Combínalos sin espacios o carácteres especiales. Ejemplo: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
	    return "No recuerdo haberte dado información sobre alguna canción...";
	}

	@Override
	public String tryWithMods() {
		return "¡Prueba con algunos mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Prueba este mapa con " + Mods.toShortNamesContinuous(mods);
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
		return "Tu nombre me esta confundiendo. ¿Estás baneado? Si no, por favor [https://github.com/Tillerino/Tillerinobot/wiki/Contact contacta con Tillerino]. (código de referencia "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Lo siento, había una hermosa secuencia de unos y ceros y me distraí. ¿Qué es lo que querías?";
	}

	@Override
	public String complaint() {
		return "Tu queja se ha archivado. Tillerino le hechará un vistazo cuando pueda.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("¡Ven aquí!");
		user.action("Abraza a " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "¡Hola! Soy el robot que mató a Tillerino y tomó el control de su cuenta. Estoy bromeando, uso la cuenta muy a menudo."
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
		return "Lo siento, actualmente " + feature + " es solo disponible para jugadores que hayan superado el rango" + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "¿A qué te refieres con nomod y mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Te he recomendado todo lo que sé."
				+ " Prueba otras opciones de recomendación o usa !reset. Si no estás seguro, prueba con !help.";
	}

	@Override
	public String notRanked() {
		return "Parece que el mapa no esta clasificado.";
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
		return "Precisión inválida: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("sasakura me ayudó a aprender Español :3");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Lo siento, pero \"" + invalid
				+ "\" no computa. Pruebo estos: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "El sintaxis para establecer un parámetro es !set opción valor. Prueba !help si necesitas más indicaciones.";
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

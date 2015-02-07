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
		return "Lo siento, no conozco ese mapa. Puede que sea muy nuevo, muy dificil, no rankeado, o no es el modo standard de osu!";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Parece que Tillerino humano dañó mi cableado interno."
				+ " Si el no lo nota, podrías [https://github.com/Tillerino/Tillerinobot/wiki/Contactar e informarle]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Que sucede? Solo recibo codigos sin sentido desde el servidor de osu!, Puedes decrime que significa? 0011101001010000"
				+ " Tillerino humano dice que no es nada por lo que haya que preocuparse, y que debemos intentar de nuevo."
				+ " Si estás demasiado preocupado por algún inconveniente, puedes [https://github.com/Tillerino/Tillerinobot/wiki/Contactarlo] y decirle. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "No hay datos del mod solicitado";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("¡Bienvenido de nuevbo!, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...Eres tu? Ha pasado tanto tiempo!");
			user.message("Es bueno tenerte de vuelta, ¿puedo recomendarte alguna canción?");
		} else {
			String[] messages = {
					"Parece que quieres una recomendación :)!",
					"Que bueno verte! :)",
					"¡Mi humano favorito! (no le digas a los otros humanos..)",
					"Que sorpresa mas linda ^.^",
					"Esperaba que aparecieras, los otros humanos son aburridos y monótonos.. pero no les digas que te lo dije! :3",
					"Que quieres hacer hoy?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "unknown command \"" + command
				+ "\". Escribe !help si necesitas ayuda.";
	}

	@Override
	public String noInformationForMods() {
		return "Lo siento, no puedo darte información sobre esos mods en este momento.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Esos mods no los conozco, puedes usar cualquier combinación de estos, DT HR HD HT EZ NC FL SO NF. Combinalos sin espacios ni carácteres especiales. Ejemplo: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "No recuerdo haberte dado ninguna información de esa canción..";
	}

	@Override
	public String tryWithMods() {
		return "Intenta este mapa con mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Intenta este mapa con" + Mods.toShortNamesContinuous(mods);
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
		return "Tu nombre me confunde.. ¿Estás baneado?.. Si no lo estpas, [https://github.com/Tillerino/Tillerinobot/wiki/Contact Contacta a Tillerino]. (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Lo siento, pensaba en esa hermosa combinación de unos y ceros infinita, ¿Que era lo que querías?";
	}

	@Override
	public String complaint() {
		return "Tu queja ha sido enviada, Tillerino lo revisará cuando tenga la ocasión.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Come here, you!");
		user.action("hugs " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "¡Hola! Soy el Robot que mató a Tillerino y se robó su cuenta.. ¡Sol bromeo!, pero si uso su cuenta.. y mucho :P"
				+ " [https://twitter.com/Tillerinobot Estado y actualizaciones]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki Comandos"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Contacto]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Preguntas Frecuentes]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Lo siento, a este punto " + feature + " Solo está disponible para personas con cierto rank. " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "A que te refieres con sin mods pero con mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "Te he recomendado todo lo que tenía en mi manga :("
				+ " Intenta otras recomendaciones, o usa la opción !reset, si no sabes como funciona, usa !help";
	}

	@Override
	public String notRanked() {
		return "Parece que este mapa no ha sido rankeado";
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
		return "Precisión inválida \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("¿Así que te gusto así como soy? :)");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Lo siento, pero \"" + invalid
				+ "\" no lo proceso, intenta con estos! " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "La sintáxis para establecer un parámetro es !set option valu si necesitas ayuda, escribe !help";
	}
}

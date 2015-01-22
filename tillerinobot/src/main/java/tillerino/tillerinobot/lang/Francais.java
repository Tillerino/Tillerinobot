package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

/**
 * French Language implementation. Thx to https://osu.ppy.sh/u/Howaitorufu for
 * this.
 */
public class Francais implements Language {

	@Override
	public String unknownBeatmap() {
		return "Je suis désolé, je trouve pas la map. Il pourrait que celle-ci sois nouvelle,"
				+ " très dure, non classée ou alors n'est pas un mode standard d'osu.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... On dirait que l'humain Tillerino à Fail sa programmation."
				+ " S'il ne s'en aperçoit pas, ou si ce n'est pas corriger, fais lui savoir S'il te plaît? @Tillerino or /u/Tillerino? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Qu'est-ce qui se passe? J'ai seulement obtenu pour le serveur osu. Peutu me dire ce que cela signifie? 0011101001010000"
				+ " L'humain Tillerino dit ce n'est rien il ne faut pas s'inquiéter, il faut encore essayer."
					+ " Si tu es inquiet pour n'importe quelle raison, contactez-moi @Tillerino or /u/Tillerino. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "aucune donnée pour le mode demandé";
	}

	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Re Salut :)");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...c'est vraiment toi? Tu étais si long :o");
			user.message("Heureux de te revoir " + apiUser.getUserName() + ". Je peux vous proposer des choses intéressantes?");
		} else {
			String[] messages = {
					"si tu veux des propositions de maps hésitent pas à me demander.",
					"tu es mon humain favori. (Dit le à personne c'est un secret!)",
					"quelle agréable surprise! ^.^",
					"j'espère  que tu vas me montrer ton niveau. Tous les autres joueurs sont faibles, mais ne leur dis pas que je l'ai dit! :3",
					"tu veux faire quoi aujourd'hui?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "commande inconnue \"" + command
			// "  "
				+ "\". Ecrit !help si tu as besoin d'aide!";
	}

	@Override
	public String noInformationForMods() {
		return "Désolé, je ne peux pas donner d'informations pour ces mods en ce moment.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Certains mods ne sont pas corrects. Les modes peuvent être : DT HR HD HT EZ NC FL SO NF. Combinez-les sans aucun espace. Exemple: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Je me rappelle pas, tu n'as eu aucune info sur les chansons...";
	}

	@Override
	public String tryWithMods() {
		return "Essaye cette map avec un mod!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Essaye cette map avec " + Mods.toShortNamesContinuous(mods);
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
		return "Ton nom m'embrouille. Es-tu banni? Sinon, contactez @Tillerino or /u/Tillerino (reference "
				+ exceptionMarker + ")";
	}

	@Override
	public String excuseForError() {
		return "Je suis désolé, il y avait cette belle séquence des 1 et des 0 mais j'ai était distrait. Que veux-tu à nouveau?";
				// " "
	}

	@Override
	public String complaint() {
		return "Ta plainte a été transmise. Tillerino s'en occupera quand il le peut.";
	}

	@Override
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Hey toi, viens ici!");
		user.action("étreint " + apiUser.getUserName());
	}

	@Override
	public String help() {
		return "Hey! Je suis le robot qui à tuer Tillerino et qui a récupéré son compte. Je rigole :p, mais j'utilise ce compte."
				+ " Vérifie sûr https://twitter.com/Tillerinobot pour le status et les mises à jour!"
				+ " Regarde là https://github.com/Tillerino/Tillerinobot/wiki pour les commandes!";
	}

	@Override
	public String faq() {
		return "Regarde https://github.com/Tillerino/Tillerinobot/wiki/FAQ pour FAQ!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Désolé, à ce point " + feature + " est seulement disponible pour les joueurs qui ont depasser le rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Que veux-tu dire NoMod avec Mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "J'ai recommendé tout ce que j'ai pensé."
				+ " Essaye d'autres options de recommandation ou l'utilisation. Si tu n'es pas sûr, tape !help.";
	}

	@Override
	public String notRanked() {
		return "Cette beatmap est pas classé.";
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
		return "Précision non valide: \"" + acc + "\"";
	}

	@Override
	public String noPercentageEstimates() {
		return "Désolé, évaluer ces informations pour le moment.";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Howaitorufu m'a appris à parler français :D");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Je suis désolé, mais \"" + invalid
				+ "\" ne calcule pas. Essaye ça " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "La commande pour définir un paramètre est !set option valeur. Ecrit !help pour avoir de l'aide.";
	}
}
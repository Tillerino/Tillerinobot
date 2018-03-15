package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * French Language implementation. Thx to https://osu.ppy.sh/u/Howaitorufu & https://osu.ppy.sh/u/ThePooN for
 * this.
 */
public class Francais implements Language {

	@Override
	public String unknownBeatmap() {
		return "Je suis désolé, je ne trouve pas la map... Il se pourrait que celle-ci soit nouvelle,"
				+ " très compliquée, non classée ou alors n'est pas un mode standard d'osu!.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... On dirait que l'humain Tillerino a foiré dans sa programmation."
				+ " S'il ne s'en rend pas compte, [https://github.com/Tillerino/Tillerinobot/wiki/Contact peux-tu lui en informer] ? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Qu'est-ce qui se passe ? Les serveurs d'osu! m'ont dit n'importe quoi... Peux-tu me dire ce que cela signifie ? 0011101001010000"
				+ " L'humain Tillerino dit que ce n'est rien, il ne faut pas s'inquiéter, il faut réessayer."
					+ " Si tu es très inquiet pour n'importe quelle raison, [https://github.com/Tillerino/Tillerinobot/wiki/Contact tu peux le contacter]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "aucune donnée pour le mod demandé.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Re ! :)");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...c'est vraiment toi ? Cela fait si longtemps ! :o"))
				.then(new Message("Heureux de te revoir " + apiUser.getUserName() + ". Puis-je te recommander quelques maps ?"));
		} else {
			String[] messages = {
					"tu as l'air de vouloir quelques recommandations !",
					"tu es mon humain favori (mais ne le dit à personne, c'est un secret !)",
					"quelle agréable surprise ! ^.^",
					"j'espère  que tu vas me montrer ton niveau. Tous les autres joueurs sont faibles, mais ne leur dit pas que j'ai dit ça ! :3",
					"comment vas-tu aujourd'hui?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "commande inconnue \"" + command
			// "  "
				+ "\". Tu peux écrire !help si tu as besoin d'aide !";
	}

	@Override
	public String noInformationForMods() {
		return "Désolé, je ne peux pas donner d'informations pour ces mods actuellement.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Certains mods ne sont pas corrects. Les modes peuvent être : DT HR HD HT EZ NC FL SO NF. Combinez-les sans aucun espace. Exemple : !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Je ne me souviens pas que tu m'ais demandé des infos sur une map...";
	}

	@Override
	public String tryWithMods() {
		return "Essaye cette map avec des mods !";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Essaye cette map avec " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Je suis désolé, il y avait cette belle séquence de 1 et de 0 et j'ai était distrait. Que veux-tu à nouveau ?";
				// " "
	}

	@Override
	public String complaint() {
		return "Ta plainte a été transmise. Tillerino s'en occupera quand il le pourra.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Hey toi, viens ici!")
			.then(new Action("câline " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Hey! Je suis le robot qui a tué Tillerino et qui a récupéré son compte. Je rigole :P, mais j'utilise beaucoup ce compte quand même."
				+ " [https://twitter.com/Tillerinobot Statut et mises à jours]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki Toutes les commandes]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Contacter Tillerino]";
	}

	@Override
	public String faq() {
		return "Va voir [https://github.com/Tillerino/Tillerinobot/wiki/FAQ la FAQ] !";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Désolé, mais actuellement " + feature + " est seulement disponible pour les joueurs qui ont depassé le rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Euh... NoMods avec des mods ? Y a pas un petit problème par ici ?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " J'ai recommendé tout ce que j'ai pu...]"
				+ " Essaye d'autres options de recommandation, ou utilise !reset. Si tu n'es pas sûr, tape !help !";
	}

	@Override
	public String notRanked() {
		return "Cette beatmap n'est pas classé.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Accuracy non valide : \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Howaitorufu, ThePooN et Pweenzor m'ont appris à parler Français ! :D");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Je suis désolé, mais \"" + invalid
				+ "\" ne fonctionne pas. Essaye ça : " + choices + " !";
	}

	@Override
	public String setFormat() {
		return "La commande pour définir un paramètre est !set option valeur. Ecrit !help pour avoir de l'aide.";
	}
	
	@Override
	public String apiTimeoutException() {
		return "Les serveurs d'osu! sont supers lents ! Je ne peux rien faire pour toi en ce moment.";
	}
	
	@Override
	public String noRecentPlays() {
		return "Je ne t'ai pas vu beaucoup jouer ces derniers temps.";
	}
	
	@Override
	public String isSetId() {
		return "Ce n'est pas une seule beatmap, mais un ensemble.";
	}
}

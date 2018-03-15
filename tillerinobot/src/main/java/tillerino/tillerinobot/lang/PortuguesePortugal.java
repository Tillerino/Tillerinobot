package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author [HDHR] andre.pedrocv@gmail.com https://osu.ppy.sh/u/3547567 https://github.com/ThatGuy986
 */
public class PortuguesePortugal implements Language {

	@Override
	public String unknownBeatmap() {
		return "Desculpe, não conheço este mapa. Pode ser muito novo, muito difícil, não estar ranqueado ou não ser do modo Osu!Standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Ugh... Parece que o Tillerino humano trocou me os fios."
				+ " se ele não reparar podes por favor [https://github.com/Tillerino/Tillerinobot/wiki/Contact informá-lo]? (reference "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "O que se passa? Estou só a receber dados estranhos do servidor do Osu!. Consegues dizer me o que significa? 0011101001010000"
				+ " O Tillerino humano disse que não é nada preocupante e que devíamos tentar outra vez."
				+ " Se estás muito preocupado com algum problema, podes [https://github.com/Tillerino/Tillerinobot/wiki/Contact avisá-lo]. (reference "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "sem dados para os mods pedidos.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Olá, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...és mesmo tu? Há quanto tempo!"))
				.then(new Message("Que bom ter-te de volta! Interessado em alguma recomendação?"));
		} else {
			String[] messages = {
					"parece que queres uma recomendação.",
					"Que bom ver-te! :)",
					"o meu humano favorito. (Não digas aos outros humanos!)",
					"que surpresa agradável! ^.^",
					"Estava na esperança que aparecesses. Todos os outros humanos são uma seca, mas não lhes digas que te disse isto! :3",
					"O que te apetece fazer hoje?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Comando Inválido \"" + command
				+ "\". Digite !help se precisar de ajuda!";
	}

	@Override
	public String noInformationForMods() {
		return "Desculpa mas de momento não posso providenciar informação para esses mods.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Esses mods não parecem estar corretos. Os mods podem ser qualquer combinação de DT HR HD HT EZ NC FL SO NF. Combina os sem espaços ou caracteres especiais. Exemplo: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Não me lembro de nenhuma informação da musica anterior...";
	}

	@Override
	public String tryWithMods() {
		return "Tenta este mapa com alguns mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Tenta este mapa com " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Desculpa, havia uma sequência de uns e zeros e distrai-me. O que querias?";
	}

	@Override
	public String complaint() {
		return "A tua reclamação foi enviada. O Tillerino irá vê-la assim que puder.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("anda cá!")
			.then(new Action("abraça " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Olá! Sou o robô que matou o Tillerino e roubou-lhe a conta. Nem por isso :3, mas uso a conta bastante."
				+ " [https://twitter.com/Tillerinobot status e atualizações]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki comandos]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact contactos]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Perguntas frequentes]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Desculpa, de momento" + feature + " só está disponível para jogadores de rank superior a " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "O que queres dizer com nomod com mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Já recomendei tudo o que consigo pensar]."
				+ " Tenta outra opção de recomendação ou usa !reset. Se não tens a certeza, experimenta !help.";
	}

	@Override
	public String notRanked() {
		return "Parece que este mapa não está ranqueado.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Precisão inválida: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("O [HDHR] ensinou-me a falar Português (de Portugal)!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Desculpa, mas \"" + invalid
				+ "\" não funciona. Tenta: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "A sintaxe para definir um parâmetro é !set opção. Tenta !help se precisares de ajuda.";
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
}

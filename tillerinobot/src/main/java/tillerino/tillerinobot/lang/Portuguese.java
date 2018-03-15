package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

public class Portuguese extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	static final Random rnd = new Random();

	@Override
	public String unknownBeatmap() {
		return "Desculpe, não conheço este mapa. Ele pode ser muito novo, muito difícil, não ranqueado ou não é um mapa do modo osu!standard.";
	}

	@Override
	public String internalException(String marker) {
		return "Aff... Parece que o Tillerino de verdade bagunçou com minha programação."
				+ " Se ele não perceber logo, poderia por favor informá-lo? [https://twitter.com/Tillerinobot @Tillerinobot] ou [http://www.reddit.com/message/compose/?to=Tillerino /u/Tillerino]? (referência "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "O que é isso? Só estou recebendo bobagens do servidor do osu. Você pode me dizer o que isso significa? 0011101001010000"
				+ " O Tillerino de verdade disse que isso não é nada preocupante e que a gente devia tentar de novo."
				+ "Se por algum motivo você está muito preocupado, você pode dizer pra ele? [https://twitter.com/Tillerinobot @Tillerinobot] ou [http://www.reddit.com/message/compose/?to=Tillerino /u/Tillerino]. (referência "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "Sem dados para os mods pedidos.";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("beep boop");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Olá," + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...é você mesmo? Faz tanto tempo!"))
				.then(new Message("Que bom que você voltou! Estaria interessado em uma recomendação?"));
		} else {
			String[] messages = {
					"parece que você gostaria de uma recomendação.",
					"que bom ver você! :)",
					"meu humano favorito. (Não diga pros outros humanos!)",
					"que surpresa agradável! ^.^",
					"Eu estava esperando que você aparecesse. Todos os outros humanos são chatos, mas não conte pra eles que eu disse isso! :3",
					"o que você está a fim de fazer hoje?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "comando desconhecido \"" + command
				+ "\". Digite !help se você precisa de ajuda!";
	}

	@Override
	public String noInformationForMods() {
		return "Desculpe, mas não posso providenciar as informações para esses mods no momento.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Esses mods não parecem estar certos. Mods podem ser uma combinação de DT, HR, HD, HT, EZ, NC, FL, SO e NF. Combine eles sem espaços ou caracteres especiais. Exemplo: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "Eu não lembro de nenhuma informação de música anterior...";
	}

	@Override
	public String tryWithMods() {
		return "Tente esse mapa com alguns mods!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Tente esse mapa com " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Desculpe, mas eu vi uma sequência linda de uns e zeros e eu me distraí. O que você queria mesmo?";
	}

	@Override
	public String complaint() {
		return "Sua reclamação foi arquivada. O Tillerino vai dar uma olhada no problema quando puder.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Vem aqui!")
			.then(new Action("abraça " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Olá! Sou o robô que matou o Tillerino de verdade e estou usando a conta dele. Brincadeira, mas ainda estou usando a conta."
				+ " Dê uma olhada em https://twitter.com/Tillerinobot para novidades e status atual."
				+ " Veja https://github.com/Tillerino/Tillerinobot/wiki para ver os comandos!";
	}

	@Override
	public String faq() {
		return "Veja https://github.com/Tillerino/Tillerinobot/wiki/FAQ para as perguntas frequentes!";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Desculpe, mas no momento " + feature + " só está disponível para jogadores que passaram do rank " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "O que você quer dizer com nomods com mods?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Eu já recomendei tudo que eu consigo pensar]."
				+ " Tente outras opções de recomendação ou use !reset. Se você não tem certeza, digite !help.";
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
		return new Message("[https://osu.ppy.sh/u/ragingalien RagingAlien], [https://osu.ppy.sh/u/wow wow] e [https://osu.ppy.sh/u/Kanegae Kanegae] me ajudaram a aprender português! :D");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "Desculpe, mas \"" + invalid
				+ "\" não computa. Tente esses: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "A sintaxe para definir um parâmetro é !set valor. Tente !help se você precisa de mais ajuda";
	}
	
	StringShuffler apiTimeoutShuffler = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		registerModification();
		final String message = "O servidor do osu! está muito lento, não há nada que posso fazer no momento.";
		return message + apiTimeoutShuffler.get(
				"Então... Quando foi a última vez que você falou com a sua avó?",
				"Que tal você ir limpar seu quarto e depois tentar de novo?",
				"Aposto que você adoraria ir caminhar agora. Sabe... lá fora?",
				"Eu sei que você tem um monte de coisas pra fazer. Que tal fazê-las agora?",
				"Você parece precisar de uma soneca.",
				"Mas dê uma olhada nesse artigo super interessante na [https://pt.wikipedia.org/wiki/Special:Random wikipédia]!",
				"Vamos ver se tem alguém bom fazendo uma [http://www.twitch.tv/directory/game/Osu! livestream] agora!",
				"Olha, um outro [http://dagobah.net/flash/Cursor_Invisible.swf jogo] que você provavelmente não vai conseguir jogar bem!",
				"Isso deve te dar um tempo pra dar uma olhada no [https://github.com/Tillerino/Tillerinobot/wiki meu manual].",
				"Relaxa, tenho uns [https://www.reddit.com/r/osugame dank memes] pra você passar o tempo.",
				"Enquanto você está aí, entediado, tente jogar [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Perguntinha: se seu disco rígido bugasse nesse exato momento, você iria perder muita coisa importante?",
				"Então... por acaso você já tentou o [https://www.google.com.br/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"Você pode ir tentar fazer outra coisa, ou podemos ficar olhando um nos olhos do outro. Em silêncio..."
				);
	}
	
	@Override
	public String noRecentPlays() {
		return "Eu não tenho visto você jogar ultimamente";
	}
	
	@Override
	public String isSetId() {
		return "Isto faz referência a um conjunto de mapas, não à apenas um.";
	}
}

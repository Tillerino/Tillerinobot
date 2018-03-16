package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;

/**
 * @author https://github.com/firedigger https://osu.ppy.sh/u/firedigger
 */
public class Russian implements Language {

	@Override
	public String unknownBeatmap() {
		return "Не могу распознать карту. Возможные причины: она слишком новая, очень сложная, еще не ранкнута или использует не osu!standard режим.";
	}

	@Override
	public String internalException(String marker) {
		return "Пф... Похоже, что настоящий Tillerino сломал мою проводку."
				+ " Если он не заметит этого в ближайшее время, не могли бы вы [https://github.com/Tillerino/Tillerinobot/wiki/Contact сообщить ему об этом]? (назовите код "
				+ marker + ")";
	}

	@Override
	public String externalException(String marker) {
		return "Что происходит? Я получаю лишь шум с сервера osu!. Можете мне объяснить, чтобы это могло значить? 0011101001010000"
				+ " Настоящий Tillerino утверждает, что не о чем беспокоиться и стоит попробовать еще раз."
				+ " Тем не менее, если вы сильно обеспокоены, вы можете [https://github.com/Tillerino/Tillerinobot/wiki/Contact сообщить ему] об этом. (назовите код "
				+ marker + ")";
	}

	@Override
	public String noInformationForModsShort() {
		return "недостаточно данных для выбранных модов";
	}

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			return new Message("бип боп");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			return new Message("Рад снова вас видеть, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			return new Message(apiUser.getUserName() + "...")
				.then(new Message("...это действительно вы? Столько времени прошло!"))
				.then(new Message("Здорово, что вы вернулись. Могу ли я заинтересовать вас рекомендацией?"));
		} else {
			String[] messages = {
					"Полагаю, вы хотите рекомендацию",
					"Как приятно снова видеть вас! :)",
					"мой любимый пользователь. (Только никому не говорите!)",
					"какой приятный сюрприз! ^.^",
					"Я надеялся, что вы появитесь. Все остальные пользователи не настолько классные, только не говорите им, что я это сказал. :3",
					"на что вы сегодня настроены?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			return new Message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "неизвестная команда \"" + command
				+ "\". Введите !help для помощи";
	}

	@Override
	public String noInformationForMods() {
		return "К сожалению, я не могу предоставить информацию для этих модов сейчас.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "Эти моды не выглядят правильно. Моды могут являться любой комбинацией из DT HR HD HT EZ NC FL SO NF. Соедините их без пробелов и других символов. Пример: !with HDHR, !with DTEZ";
	}

	@Override
	public String noLastSongInfo() {
		return "В моей памяти нет песни от вашего последнего запроса...";
	}

	@Override
	public String tryWithMods() {
		return "Попробуйте эту карту с модами!";
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Попробуйте эту карту с " + Mods.toShortNamesContinuous(mods);
	}

	@Override
	public String excuseForError() {
		return "Извините, я отвлекся на эту удивительную последовательность из нулей и единиц. Чем я могу быть полезен?";
	}

	@Override
	public String complaint() {
		return "Ваша жалоба принята. Tillerino рассмотрит ее при первой возможности.";
	}

	@Override
	public Response hug(OsuApiUser apiUser) {
		return new Message("Подойдите, ну же!")
			.then(new Action("обнимает " + apiUser.getUserName()));
	}

	@Override
	public String help() {
		return "Приветствую! Я робот, который убил Tillerino и захватил его аккаунт. Шучу, однако я все еще использую его аккаунт."
				+ " [https://twitter.com/Tillerinobot статус и обновления]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki команды]"
				+ " - [http://ppaddict.tillerino.org/ ppaddict]"
				+ " - [https://github.com/Tillerino/Tillerinobot/wiki/Contact Обратная связь]";
	}

	@Override
	public String faq() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ Часто задаваемые вопросы]";
	}
	
	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "К сожалению, " + feature + " доступна лишь игрокам с рангом выше " + minRank + ".";
	}
	
	@Override
	public String mixedNomodAndMods() {
		return "Зачем вы используете nomod вместе с модами?";
	}
	
	@Override
	public String outOfRecommendations() {
		return "[https://github.com/Tillerino/Tillerinobot/wiki/FAQ#the-bot-says-its-out-of-recommendations-what-do"
				+ " Я уже порекомендовал все, что знал]."
				+ " Попробуйте другие опции рекомендаций или используйте !reset. Если вы не уверены, что делать, посмотрите справку !help.";
	}

	@Override
	public String notRanked() {
		return "Похоже, что эта карта еще не ранкнута.";
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Недопустимое значение accuracy: \"" + acc + "\"";
	}

	@Override
	public Response optionalCommentOnLanguage(OsuApiUser apiUser) {
		return new Message("Вы изъявили желание общаться на русском (Перевод сделан [https://osu.ppy.sh/u/firedigger firedigger])");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "К сожалению, \"" + invalid
				+ "\" не подходит. Попробуйте эти: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Синтаксис для выставления параметров: !set option value. Воспользуйтесь справкой !help если вам нужны подсказки.";
	}
	
	@Override
	public String apiTimeoutException() {
		return "Сервера осу сейчас довольно медленные, увы, я не могу ничем вам помочь в этот раз.";
	}
	
	@Override
	public String noRecentPlays() {
		return "Давно не видел, как вы играли!";
	}
	
	@Override
	public String isSetId() {
		return "Эта ссылка указывает на набор карт, а не одну конкретную.";
	}
}

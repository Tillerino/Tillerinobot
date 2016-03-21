package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

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
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		if(inactiveTime < 60 * 1000) {
			user.message("бип боп");
		} else if(inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Рад снова вас видеть, " + apiUser.getUserName() + ".");
		} else if(inactiveTime > 7l * 24 * 60 * 60 * 1000) {
			user.message(apiUser.getUserName() + "...");
			user.message("...это действительно вы? Столько времени прошло!");
			user.message("Здорово, что вы вернулись. Могу ли я заинтересовать вас рекомендацией?");
		} else {
			String[] messages = {
					"Полагаю, вы хотите рекомендацию",
					"Как приятно снова видеть вас! :)",
					"Мой любимый пользователь. (Только никому не говорите!)",
					"Какой приятный сюрприз! ^.^",
					"Я надеялся, что вы появитесь. Все остальные пользователи не настолько классные, только не говорите им, что я это сказал. :3",
					"На что вы сегодня настроены?",
			};
			
			Random random = new Random();
			
			String message = messages[random.nextInt(messages.length)];
			
			user.message(apiUser.getUserName() + ", " + message);
		}
	}

	@Override
	public String unknownCommand(String command) {
		return "Неизвестная команда \"" + command
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
		return "Ваш ник меня настораживает. Вас забанили? Если нет, пожалуйста [https://github.com/Tillerino/Tillerinobot/wiki/Contact свяжитесь с Tillerino]. (назовите код "
				+ exceptionMarker + ")";
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
	public void hug(final IRCBotUser user, OsuApiUser apiUser) {
		user.message("Подойдите, ну же!");
		user.action("обнимает " + apiUser.getUserName());
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
		return "Недопустимое значение accuracy: \"" + acc + "\"";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("Вы изъявили желание общаться на русском (Перевод сделан [https://osu.ppy.sh/u/firedigger firedigger] и [https://osu.ppy.sh/u/a1mighty a1mighty])");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "К сожалению, \"" + invalid
				+ "\" не подходит. Попробуйте эти: " + choices + "!";
	}

	@Override
	public String setFormat() {
		return "Синтаксис для выставления параметров: !set option value. Воспользуйтесь справкой !help, если вам нужны подсказки.";
	}
	
	StringShuffler doSomething = new StringShuffler(rnd);
	
	@Override
	public String apiTimeoutException() {
		final String message = "Сервера osu! сейчас работают очень медленно, и пока что я вряд ли смогу что-то сделать. ";
		return message + doSomething.get(
				"Послушайте... А когда вы последний раз разговаривали со своей бабушкой?",
				"Как насчет убраться у себя в комнате, а потом попробовать ещё раз?",
				"Готов поспорить, что вам сейчас хочется прогуляться. Вы понимаете... по улице?",
				"Мне кажется, что у вас есть какие-то дела? Почему бы не заняться ими сейчас?",
				"Почему бы вам не вздремнуть?",
				"Как насчет того, чтобы взглянуть на эту очень интересную страницу в [https://ru.wikipedia.org/wiki/Special:Random Википедии]?",
				"Давайте лучше посмотрим, вдруг кто-то крутой сейчас [http://www.twitch.tv/directory/game/Osu! стримит]!",
				"Зато у вас есть время изучить [https://github.com/Tillerino/Tillerinobot/wiki мою инструкцию].",
				"Не грустите, возможно, вы сможете скоротать время за чтением новой интересной темы в [https://www.reddit.com/r/osugame Reddit-сообществе] osu!.",
				"Если скучно, попробуйте игру [http://gabrielecirulli.github.io/2048/ 2048]!",
				"Шуточный вопрос: если ваш жесткий диск сломается прямо сейчас, сколько ваших данных будет потеряно навсегда?",
				"Так... А вы когда-нибудь учавствовали в [https://www.google.ru/search?q=bring%20sally%20up%20push%20up%20challenge sally up push up challenge]?",
				"Вы можете поделать ещё что-нибудь, или мы можем просто смотреть в глаза друг другу. Молча."
				);
	}

	@Override
	public String noRecentPlays() {
		return "В последнее время вы не играли.";
	}
	
	@Override
	public String isSetId() {
		return "Эта ссылка указывает на всю карту, а не на какую-либо отдельную сложность.";
	}
	
	@Override
	public String getPatience() {
		return "Секундочку...";
	}
}

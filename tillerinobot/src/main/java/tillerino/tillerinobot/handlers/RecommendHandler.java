package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

@Slf4j
public class RecommendHandler implements CommandHandler {
	BotBackend backend;
	RecommendationsManager manager;

	@Inject
	public RecommendHandler(BotBackend backend, RecommendationsManager manager) {
		super();
		this.backend = backend;
		this.manager = manager;
	}

	@Override
	public boolean handle(String command, IRCBotUser user,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException {
		Language lang = userData.getLanguage();

		String message = command.toLowerCase();

		boolean isRecommend = false;

		if (message.equals("r")) {
			isRecommend = true;
			message = "";
		}
		if (getLevenshteinDistance(message, "recommend") <= 2) {
			isRecommend = true;
			message = "";
		}
		if (message.startsWith("r ")) {
			isRecommend = true;
			message = message.substring(2);
		}
		if (message.contains(" ")) {
			int pos = message.indexOf(' ');
			if (getLevenshteinDistance(message.substring(0, pos), "recommend") <= 2) {
				isRecommend = true;
				message = message.substring(pos + 1);
			}
		}
		if (!isRecommend) {
			return false;
		}

		Recommendation recommendation = manager.getRecommendation(apiUser,
				message, lang);
		BeatmapMeta beatmap = recommendation.beatmap;

		if (beatmap == null) {
			user.message(lang.excuseForError());
			log.error("unknow recommendation occurred");
			return true;
		}
		String addition = null;
		if (recommendation.bareRecommendation.getMods() < 0) {
			addition = lang.tryWithMods();
		}
		if (recommendation.bareRecommendation.getMods() > 0
				&& beatmap.getMods() == 0) {
			addition = lang.tryWithMods(Mods
					.getMods(recommendation.bareRecommendation.getMods()));
		}

		if (user.message(beatmap.formInfoMessage(true, addition,
				userData.getHearts(), null))) {
			userData.setLastSongInfo(new BeatmapWithMods(beatmap.getBeatmap()
					.getBeatmapId(), beatmap.getMods()));
			backend.saveGivenRecommendation(apiUser.getUserId(), beatmap
					.getBeatmap().getBeatmapId(), recommendation.bareRecommendation
					.getMods());

			lang.optionalCommentOnRecommendation(user, apiUser, recommendation);
		}

		return true;
	}

}

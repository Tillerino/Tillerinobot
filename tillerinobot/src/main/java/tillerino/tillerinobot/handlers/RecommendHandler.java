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

		String lowerCase = command.toLowerCase();

		boolean isRecommend = false;

		if (lowerCase.equals("r")) {
			isRecommend = true;
			command = "";
		}
		if (getLevenshteinDistance(lowerCase, "recommend") <= 2) {
			isRecommend = true;
			command = "";
		}
		if (lowerCase.startsWith("r ")) {
			isRecommend = true;
			command = command.substring(2);
		}
		if (lowerCase.contains(" ")) {
			int pos = lowerCase.indexOf(' ');
			if (getLevenshteinDistance(lowerCase.substring(0, pos), "recommend") <= 2) {
				isRecommend = true;
				command = command.substring(pos + 1);
			}
		}
		if (!isRecommend) {
			return false;
		}

		Recommendation recommendation = manager.getRecommendation(apiUser,
				command, lang);
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
				&& beatmap.getMods() != recommendation.bareRecommendation.getMods()) {
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

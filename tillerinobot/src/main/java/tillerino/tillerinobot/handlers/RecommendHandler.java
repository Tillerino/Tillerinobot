package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
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
	public boolean handle(final String originalCommand, IRCBotUser user,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException, InterruptedException {
		Language lang = userData.getLanguage();

		String lowerCase = originalCommand.toLowerCase();

		final String command;
		searchRecommend: {
			if (lowerCase.equals("r")) {
				command = "";
				break searchRecommend;
			}
			if (getLevenshteinDistance(lowerCase, "recommend") <= 2) {
				command = "";
				break searchRecommend;
			}
			if (lowerCase.startsWith("r ")) {
				command = originalCommand.substring(2);
				break searchRecommend;
			}
			if (lowerCase.contains(" ")) {
				int pos = lowerCase.indexOf(' ');
				if (getLevenshteinDistance(lowerCase.substring(0, pos), "recommend") <= 2) {
					command = originalCommand.substring(pos + 1);
					break searchRecommend;
				}
			}
			return false;
		}

		Recommendation recommendation = manager.getRecommendation(apiUser,
				command, lang);
		BeatmapMeta beatmap = recommendation.beatmap;

		if (beatmap == null) {
			log.error("unknow recommendation occurred");
			throw new RareUserException(lang.excuseForError());
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
				userData.getHearts(), null, null, null))) {
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

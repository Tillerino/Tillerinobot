package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.Recommendation;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

@Slf4j
public class RecommendHandler extends CommandHandler.WithShorthand {
	public static final String MDC_FLAG = "r";

	RecommendationsManager manager;

	@Inject
	public RecommendHandler(RecommendationsManager manager) {
		super("recommend");
		this.manager = manager;
	}

	@Override
	public Response handleArgument(String remaining, OsuApiUser apiUser,
								   UserData userData) throws UserException,
			IOException, SQLException, InterruptedException {
		MDC.put(IRCBot.MDC_HANDLER, MDC_FLAG);

		Language lang = userData.getLanguage();

		if (remaining.isEmpty() && userData.getDefaultRecommendationOptions() != null) {
			remaining = userData.getDefaultRecommendationOptions();
		}

		Recommendation recommendation = manager.getRecommendation(apiUser,
				remaining, lang);
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

		return new Success(beatmap.formInfoMessage(true, addition,
						userData.getHearts(), null, null, null))
				.thenRun(() -> 
					userData.setLastSongInfo(new BeatmapWithMods(beatmap
							.getBeatmap().getBeatmapId(), beatmap.getMods())))
				.thenRunAsync(() -> 
					manager.saveGivenRecommendation(apiUser.getUserId(),
							beatmap.getBeatmap().getBeatmapId(),
							recommendation.bareRecommendation.getMods())).
				then(lang.optionalCommentOnRecommendation(apiUser, recommendation));
	}

}

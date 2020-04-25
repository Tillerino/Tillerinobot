package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler;
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

	private final RecommendationsManager manager;
	private final LiveActivity liveActivity;

	@Inject
	public RecommendHandler(RecommendationsManager manager, LiveActivity liveActivity) {
		super("recommend");
		this.manager = manager;
		this.liveActivity = liveActivity;
	}

	@Override
	public GameChatResponse handleArgument(String originalCommand, @Nonnull String remaining, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException, InterruptedException {
		MDC.put(MdcUtils.MDC_HANDLER, MDC_FLAG);

		Language lang = userData.getLanguage();

		if (remaining.isEmpty()) {
			remaining = userData.getDefaultRecommendationOptions();
		}
		if (remaining == null) {
			remaining = "";
		}

		{
			/*
			 * First we check if the command is proper and then broadcast it. The point of
			 * checking if it is correct is to make sure that nothing else is contained in
			 * the message making the message anonymous as can be.
			 */
			manager.parseSamplerSettings(apiUser, remaining, lang);
			MdcUtils.getEventId().ifPresent(eventId -> liveActivity.propagateMessageDetails(eventId, "!" + originalCommand));
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

		userData.setLastSongInfo(new BeatmapWithMods(beatmap
				.getBeatmap().getBeatmapId(), beatmap.getMods()));
		manager.saveGivenRecommendation(apiUser.getUserId(),
				beatmap.getBeatmap().getBeatmapId(),
				recommendation.bareRecommendation.getMods());
		return new Success(beatmap.formInfoMessage(true, addition,
						userData.getHearts(), null, null, null))
				.then(lang.optionalCommentOnRecommendation(apiUser, recommendation));
	}

}

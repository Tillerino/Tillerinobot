package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;

import lombok.Value;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

@Value
public class RecentHandler implements CommandHandler {
	BotBackend backend;

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language language)
			throws UserException, IOException, SQLException, InterruptedException {
		if (!command.equalsIgnoreCase("now")) {
			return null;
		}
		
		if(userData.getHearts() <= 0) {
			return null;
		}

		List<OsuApiScore> recentPlays = backend.getRecentPlays(apiUser.getUserId());
		if (recentPlays.isEmpty()) {
			throw new UserException(language.noRecentPlays());
		}

		OsuApiScore score = recentPlays.get(0);

		final BeatmapMeta estimates = backend.loadBeatmap(score.getBeatmapId(), score.getMods(), language);

		if (estimates == null) {
			throw new UserException(language.unknownBeatmap());
		}
		if (estimates.getMods() != score.getMods()) {
			throw new UserException(language.noInformationForMods());
		}

		userData.setLastSongInfo(estimates.getBeatmapWithMods());
		return new Success(estimates.formInfoMessage(false, null,
				userData.getHearts(), score.getAccuracy(), null, null));
	}

}

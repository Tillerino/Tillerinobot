package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import lombok.Value;

import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;

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
	public Response handle(String command, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException, InterruptedException {
		if (!command.equalsIgnoreCase("now")) {
			return null;
		}
		
		if(userData.getHearts() <= 0) {
			return null;
		}
		
		final Language language = userData.getLanguage();

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

		return new Success(estimates.formInfoMessage(false, null,
				userData.getHearts(), score.getAccuracy(), null, null))
				.thenRun(() -> userData.setLastSongInfo(estimates
						.getBeatmapWithMods()));
	}

}

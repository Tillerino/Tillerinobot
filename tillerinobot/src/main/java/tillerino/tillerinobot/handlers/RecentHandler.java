package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.lang.Language;

public record RecentHandler(PullThrough pullThrough, DiffEstimateProvider diffEstimateProvider)
        implements CommandHandler {
    @Override
    public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language language)
            throws UserException, IOException, SQLException, InterruptedException {
        if (!command.equalsIgnoreCase("now")) {
            return null;
        }

        if (userData.getHearts() <= 0) {
            return null;
        }

        List<ApiScore> recentPlays = pullThrough.getRecentPlays(apiUser.getUserId());
        if (recentPlays.isEmpty()) {
            throw new UserException(language.noRecentPlays());
        }

        OsuApiScore score = recentPlays.getFirst();

        final BeatmapMeta estimates = diffEstimateProvider.loadBeatmap(score.getBeatmapId(), score.getMods());

        if (estimates == null) {
            throw new UserException(language.unknownBeatmap());
        }
        if (estimates.getMods() != score.getMods()) {
            throw new UserException(language.noInformationForMods());
        }

        userData.setLastSongInfo(estimates.getBeatmapWithMods());
        return new Success(
                estimates.formInfoMessage(false, true, null, userData.getHearts(), score.getAccuracy(), null, null));
    }
}

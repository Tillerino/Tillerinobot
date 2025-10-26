package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.Recommendation;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComplaintHandler implements CommandHandler {
    private final RecommendationsManager manager;

    @Override
    public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language lang) {
        if (getLevenshteinDistance(
                        command.toLowerCase().substring(0, Math.min("complain".length(), command.length())), "complain")
                <= 2) {
            Recommendation lastRecommendation = manager.getLastRecommendation(apiUser.getUserId());
            if (lastRecommendation != null && lastRecommendation.beatmap() != null) {
                log.debug(
                        "COMPLAINT: {} mods: {}. Recommendation source: {}",
                        lastRecommendation.beatmap().getBeatmap().getBeatmapId(),
                        lastRecommendation.bareRecommendation().mods(),
                        Arrays.asList(ArrayUtils.toObject(
                                lastRecommendation.bareRecommendation().causes())));
                return new Success(lang.complaint());
            }
        }
        return null;
    }
}

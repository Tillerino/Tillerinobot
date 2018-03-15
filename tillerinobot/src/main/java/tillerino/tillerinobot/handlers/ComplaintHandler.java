package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.recommendations.Recommendation;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.UserException;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComplaintHandler implements CommandHandler {	
	private final RecommendationsManager manager;
	
	@Override
	public Response handle(String command, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException,
			InterruptedException {
		if(getLevenshteinDistance(command.toLowerCase().substring(0, Math.min("complain".length(), command.length())), "complain") <= 2) {
			Recommendation lastRecommendation = manager
					.getLastRecommendation(apiUser.getUserId());
			if(lastRecommendation != null && lastRecommendation.beatmap != null) {
				log.debug("COMPLAINT: " + lastRecommendation.beatmap.getBeatmap().getBeatmapId() + " mods: " + lastRecommendation.bareRecommendation.getMods() + ". Recommendation source: " + Arrays.asList(ArrayUtils.toObject(lastRecommendation.bareRecommendation.getCauses())));
				return new Success(userData.getLanguage().complaint());
			}
		}
		return null;
	}

}

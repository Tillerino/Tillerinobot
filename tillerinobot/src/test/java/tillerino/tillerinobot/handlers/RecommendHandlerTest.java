package tillerino.tillerinobot.handlers;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.LiveActivity;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Recommendation;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class RecommendHandlerTest {
	@Test
	public void testDefaultSettings() throws Exception {
		RecommendationsManager manager = mock(RecommendationsManager.class);
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		beatmap.setMaxCombo(100);
		when(manager.getRecommendation(any(), any(), any())).thenReturn(
				new Recommendation(new BeatmapMeta(beatmap, null, mock(PercentageEstimates.class)), new BareRecommendation(0, 0, null, null, 0)));
		UserData userData = mock(UserData.class);

		when(userData.getDefaultRecommendationOptions()).thenReturn("dt");
		new RecommendHandler(manager, mock(LiveActivity.class)).handle("r", mock(OsuApiUser.class), userData, new Default());
		verify(manager).getRecommendation(any(), eq("dt"), any());
	}
}
